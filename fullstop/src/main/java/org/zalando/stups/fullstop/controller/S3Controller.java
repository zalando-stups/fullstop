/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.zalando.stups.fullstop.controller;

import static org.slf4j.LoggerFactory.getLogger;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.List;

import org.slf4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.zalando.stups.fullstop.PluginEventsProcessor;
import org.zalando.stups.fullstop.filereader.FileEventReader;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.cloudtrail.processinglibrary.exceptions.CallbackException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/**
 * Created by gkneitschel.
 */
@RestController
@RequestMapping(value = "/s3", produces = APPLICATION_JSON_VALUE)
public class S3Controller {

    public static final String JSON_GZ = ".json.gz";
    private final Logger log = getLogger(getClass());

    @Value("${fullstop.processor.properties.s3Region}")
    private String s3Region;

    @Value("${fullstop.plugin.properties.fullstopHome}")
    private String fullstopHome;

    @Autowired
    private PluginEventsProcessor pluginEventsProcessor;

    @RequestMapping(method = GET, value = "/read")
    public void fetchS3() throws CallbackException, FileNotFoundException {

        log.info("Reading fullstop directory here: {}", fullstopHome);

        File directory = new File(fullstopHome);

        File[] files;

        try {
            files = directory.listFiles();
        } catch (Exception e) {
            throw new FileNotFoundException("You should download the file before read these.");
        }

        if (files == null) {
            throw new FileNotFoundException("Directory is empty");
        }

        for (File file : files) {
            log.info("Process file: {}", file.getAbsolutePath());

            FileEventReader reader = new FileEventReader(pluginEventsProcessor);
            reader.readEvents(file, null);
        }
    }

    @RequestMapping(method = GET, value = "/download")
    public void downloadFiles(@RequestParam(value = "bucket") final String bucket,
                              @RequestParam(value = "location") final String location,
                              @RequestParam(value = "page") final int page) {

        try {
            log.info("Creating fullstop directory here: {}", fullstopHome);

            boolean mkdirs = new File(fullstopHome).mkdirs();
        } catch (SecurityException e) {
            // do nothing
        }

        AmazonS3Client amazonS3Client = new AmazonS3Client();
        amazonS3Client.setRegion(Region.getRegion(Regions.fromName(s3Region)));

        ListObjectsRequest listObjectsRequest =
                new ListObjectsRequest().withBucketName(bucket)                //
                        .withPrefix(location)                                  //
                        .withMaxKeys(page);

        ObjectListing objectListing = amazonS3Client.listObjects(listObjectsRequest);

        final List<S3ObjectSummary> s3ObjectSummaries = objectListing.getObjectSummaries();

        while (objectListing.isTruncated()) {

            objectListing = amazonS3Client.listNextBatchOfObjects(objectListing);
            s3ObjectSummaries.addAll(objectListing.getObjectSummaries());

        }

        for (S3ObjectSummary s3ObjectSummary : s3ObjectSummaries) {
            String bucketName = s3ObjectSummary.getBucketName();
            String key = s3ObjectSummary.getKey();

            S3Object object = amazonS3Client.getObject(new GetObjectRequest(bucketName, key));
            InputStream inputStream = object.getObjectContent();

            File file = new File(fullstopHome, object.getBucketName() + object.getObjectMetadata().getETag() + JSON_GZ);

            copyInputStreamToFile(inputStream, file);
            log.info("File saved here: {}", file.getAbsolutePath());

        }
    }

    private void copyInputStreamToFile(final InputStream in, final File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
