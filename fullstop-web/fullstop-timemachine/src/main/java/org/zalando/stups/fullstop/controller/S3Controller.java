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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.stups.fullstop.CloudTrailProcessingLibraryProperties;
import org.zalando.stups.fullstop.PluginEventsProcessor;
import org.zalando.stups.fullstop.filereader.FileEventReader;

import java.io.*;
import java.util.List;

/**
 * Created by gkneitschel.
 */
@RestController
@RequestMapping(value = "/s3")
public class S3Controller {

    public static final String JSON_GZ = ".json.gz";

    private static final String S3_REGION_KEY = "s3Region";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final CloudTrailProcessingLibraryProperties cloudTrailProcessingLibraryProperties;

    private final PluginEventsProcessor pluginEventsProcessor;

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

        final File directory = new File(fullstopLoggingDir);

        final File[] files;

        try {
            files = directory.listFiles();
        } catch (final Exception e) {
            throw new FileNotFoundException("You should download the file before read these.");
        }

        if (files == null) {
            throw new FileNotFoundException("Directory is empty");
        }

        for (final File file : files) {
            log.info("Process file: {}", file.getAbsolutePath());

            final FileEventReader reader = new FileEventReader(pluginEventsProcessor);
            reader.readEvents(file, null);
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/download")
    public void downloadFiles(@RequestParam(value = "bucket") final String bucket,
                              @RequestParam(value = "location") final String location,
                              @RequestParam(value = "page") final int page) {

        try {
            log.info("Creating fullstop directory here: {}", fullstopLoggingDir);

            final boolean mkdirs = new File(fullstopLoggingDir).mkdirs();
        } catch (final SecurityException e) {
            // do nothing
        }

        final AmazonS3Client amazonS3Client = new AmazonS3Client();
        amazonS3Client.setRegion(
                Region.getRegion(
                        Regions.fromName(
                                (String) cloudTrailProcessingLibraryProperties.getAsProperties()
                                        .get(S3_REGION_KEY))));

        final ListObjectsRequest listObjectsRequest =
                new ListObjectsRequest().withBucketName(bucket) //
                        .withPrefix(location)   //
                        .withMaxKeys(page);

        ObjectListing objectListing = amazonS3Client.listObjects(listObjectsRequest);

        final List<S3ObjectSummary> s3ObjectSummaries = objectListing.getObjectSummaries();

        while (objectListing.isTruncated()) {

            objectListing = amazonS3Client.listNextBatchOfObjects(objectListing);
            s3ObjectSummaries.addAll(objectListing.getObjectSummaries());

        }

        for (final S3ObjectSummary s3ObjectSummary : s3ObjectSummaries) {
            final String bucketName = s3ObjectSummary.getBucketName();
            final String key = s3ObjectSummary.getKey();

            final S3Object object = amazonS3Client.getObject(new GetObjectRequest(bucketName, key));
            final InputStream inputStream = object.getObjectContent();

            final File file = new File(
                    fullstopLoggingDir,
                    object.getBucketName() + object.getObjectMetadata().getETag() + JSON_GZ);

            copyInputStreamToFile(inputStream, file);
            log.info("File saved here: {}", file.getAbsolutePath());

        }
    }

    private void copyInputStreamToFile(final InputStream in, final File file) {
        try {
            final OutputStream out = new FileOutputStream(file);
            final byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            out.close();
            in.close();
        } catch (final Exception e) {
            log.warn(e.getMessage(), e);
        }
    }
}
