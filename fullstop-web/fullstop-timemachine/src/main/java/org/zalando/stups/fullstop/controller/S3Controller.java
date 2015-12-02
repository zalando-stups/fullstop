package org.zalando.stups.fullstop.controller;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudtrail.processinglibrary.exceptions.CallbackException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.stups.fullstop.CloudTrailProcessingLibraryProperties;
import org.zalando.stups.fullstop.PluginEventsProcessor;
import org.zalando.stups.fullstop.filereader.FileEventReader;
import org.zalando.stups.fullstop.s3.S3Service;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by gkneitschel.
 */
@RestController
@RequestMapping(value = "/s3")
@PreAuthorize("#oauth2.hasScope('uid')")
public class S3Controller {

    public static final String JSON_GZ = ".json.gz";

    private static final String S3_REGION_KEY = "s3Region";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final CloudTrailProcessingLibraryProperties cloudTrailProcessingLibraryProperties;

    private PluginEventsProcessor pluginEventsProcessor;

    @Value("${fullstop.logging.dir}")
    private String fullstopLoggingDir;

    @Autowired
    public S3Controller(final PluginEventsProcessor pluginEventsProcessor,
            final CloudTrailProcessingLibraryProperties cloudTrailProcessingLibraryProperties) {
        this.pluginEventsProcessor = pluginEventsProcessor;
        this.cloudTrailProcessingLibraryProperties = cloudTrailProcessingLibraryProperties;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/read")
    public void fetchS3() throws CallbackException, FileNotFoundException {

        log.info("Reading fullstop directory here: {}", fullstopLoggingDir);

        File directory = new File(fullstopLoggingDir);

        File[] files;

        try {
            files = directory.listFiles();
        }
        catch (Exception e) {
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

    @RequestMapping(method = RequestMethod.GET, value = "/download")
    public void downloadFiles(@RequestParam(value = "bucket") final String bucket,
            @RequestParam(value = "location") final String location,
            @RequestParam(value = "page") final int page) {

        try {
            log.info("Creating fullstop directory here: {}", fullstopLoggingDir);

            boolean mkdirs = new File(fullstopLoggingDir).mkdirs();
        }
        catch (SecurityException e) {
            // do nothing
        }

        AmazonS3Client amazonS3Client = new AmazonS3Client();
        amazonS3Client.setRegion(
                Region.getRegion(
                        Regions.fromName(
                                (String) cloudTrailProcessingLibraryProperties.getAsProperties()
                                                                              .get(S3_REGION_KEY))));

        ListObjectsRequest listObjectsRequest =
                new ListObjectsRequest().withBucketName(bucket) //
                        .withPrefix(location)   //
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

            File file = new File(
                    fullstopLoggingDir,
                    object.getBucketName() + object.getObjectMetadata().getETag() + JSON_GZ);

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
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
