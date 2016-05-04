package org.zalando.stups.fullstop.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.Base64;
import com.amazonaws.util.IOUtils;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.joda.time.DateTimeZone.UTC;

@Service
public class S3Service {

    public static final String TAUPAGE_YAML = "taupage.yaml";

    public static final String AUDIT_LOG_FILE_NAME = "audit-log-";

    public static final String LOG_GZ = ".log.gz";

    private final Logger log = LoggerFactory.getLogger(S3Service.class);

    private final AmazonS3Client s3client = new AmazonS3Client();

    @Value("${fullstop.instanceData.bucketName}")
    private String bucketName;

    public String writeToS3(final String accountId, final String region, final Date instanceBootTime,
            final String logData, final String logType, final String instanceId) throws IOException {
        String fileName = null;

        final DateTime dateTime = new DateTime(instanceBootTime, UTC);

        final String keyName = Paths.get(
                accountId, region, dateTime.toString("YYYY"), dateTime.toString("MM"),
                dateTime.toString("dd"), instanceId + "-" + dateTime).toString();

        switch (LogType.valueOf(logType)) {

        case USER_DATA:
            fileName = TAUPAGE_YAML;
            break;

        case AUDIT_LOG:
            fileName = AUDIT_LOG_FILE_NAME + new DateTime(UTC) + LOG_GZ;
            break;

        default:
            log.error("Wrong logType given: " + logType);
            break;
        }

        final ObjectMetadata metadata = new ObjectMetadata();
        final byte[] decodedLogData = Base64.decode(logData);
        metadata.setContentLength(decodedLogData.length);

        final InputStream stream = new ByteArrayInputStream(decodedLogData);

        putObjectToS3(bucketName, fileName, keyName, metadata, stream);

        return Paths.get(bucketName, keyName, fileName).toString();
    }

    public void putObjectToS3(final String bucket, final String fileName, final String keyName,
            final ObjectMetadata metadata, final InputStream stream) {
        // AmazonS3 s3client = new AmazonS3Client();
        try {
            log.info("Uploading a new object to S3 from a file");

            s3client.putObject(new PutObjectRequest(bucket, Paths.get(keyName, fileName).toString(), stream, metadata));

        }
        catch (final AmazonServiceException e) {
            log.error("Could not put object to S3", e);
        }
    }

    public List<String> listCommonPrefixesS3Objects(final String bucketName, final String prefix) {
        final List<String> commonPrefixes = Lists.newArrayList();

        try {
            log.info("Listing objects in bucket '{}' with prefix '{}'", bucketName, prefix);

            final ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                    .withDelimiter("/")
                    .withBucketName(bucketName)
                    .withPrefix(prefix);

            ObjectListing objectListing;

            do {
                objectListing = s3client.listObjects(listObjectsRequest);
                objectListing.getCommonPrefixes().stream().map(S3Service::urlDecode).forEach(commonPrefixes::add);
                listObjectsRequest.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());

        } catch (final AmazonServiceException e) {
            log.error("Could not list common prefixes in S3", e);
        }

        return commonPrefixes;
    }

    private static String urlDecode(final String url) {
        try {
            return URLDecoder.decode(url, UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public List<String> listS3Objects(final String bucketName, final String prefix) {
        final List<String> s3Objects = Lists.newArrayList();

        try {
            log.info("Listing objects");

            final ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                    .withDelimiter("/")
                    .withBucketName(bucketName)
                    .withPrefix(prefix);

            ObjectListing objectListing;

            do {
                objectListing = s3client.listObjects(listObjectsRequest);
                for (final S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    if (objectSummary.getKey().equals(prefix)) {
                        continue;
                    }
                    s3Objects.add(objectSummary.getKey());
                }

                listObjectsRequest.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());

        }
        catch (final AmazonServiceException e) {
            log.error("Error Message:    " + e.getMessage());
        }

        return s3Objects;
    }

    public String downloadObject(final String bucketName, final String key) {

        final S3Object object = s3client.getObject(new GetObjectRequest(bucketName, key));

        final InputStream inputStream = object.getObjectContent();

        String result = null;
        try {
            result = IOUtils.toString(inputStream);
        }
        catch (final IOException e) {
            log.warn("Could not download file for bucket: {}, with key: {}", bucketName, key);
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (final IOException ex) {
                    log.debug("Ignore failure in closing the Closeable", ex);
                }
            }
        }

        log.info("Downloaded file for bucket: {}, with key: {}", bucketName, key);
        return result;
    }
}
