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

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by gkneitschel.
 */
@RestController
@RequestMapping(value = "/s3downloader", produces = APPLICATION_JSON_VALUE)
public class S3Downloader {

    private static final Logger LOG = LoggerFactory.getLogger(S3Downloader.class);

    @Value("${fullstop.processor.properties.s3Region}")
    private String s3Region;

    @RequestMapping(method = GET)
    public S3Object downloadFiles() {
        AmazonS3Client amazonS3Client = new AmazonS3Client();
        amazonS3Client.setRegion(Region.getRegion(Regions.fromName(s3Region)));
        ObjectListing objectListing = amazonS3Client.listObjects(new ListObjectsRequest().withBucketName("zalando-cloudtrail-logs"));
        final List<S3ObjectSummary> s3ObjectSummaries = objectListing.getObjectSummaries();

        for (S3ObjectSummary s3ObjectSummary : s3ObjectSummaries) {
            String bucketName = s3ObjectSummary.getBucketName();
            String key = s3ObjectSummary.getKey();

            S3Object object = amazonS3Client.getObject(new GetObjectRequest(bucketName, key));
            InputStream inputStream = object.getObjectContent();
            File file = new File(getClass().getResource("/logs/").getPath().concat("greg_").concat(s3ObjectSummary.getETag()));

            copyInputStreamToFile(inputStream, file);
            LOG.info("File saved here: {}", file.getAbsolutePath());

        }
        return null;

    }

    private void copyInputStreamToFile(InputStream in, File file) {
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
