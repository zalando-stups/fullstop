/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.s3;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.Base64;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Date;

import static org.joda.time.DateTimeZone.UTC;

/**
 * Created by mrandi
 */
@Service
public class S3Writer {

    public static final String TAUPAGE_YAML = "taupage.yaml";
    public static final String AUDIT_LOG_FILE_NAME = "audit-log-";
    public static final String LOG_GZ = ".log.gz";

    private static final Logger logger = LoggerFactory.getLogger(S3Writer.class);

    @Value("${fullstop.instanceData.bucketName}")
    private String bucketName;

    public void writeToS3(String accountId, String region, Date instanceBootTime, String logData, String logType,
                          String instanceId) throws IOException {
        String fileName = null;

        DateTime dateTime = new DateTime(instanceBootTime, UTC);

        String keyName = Paths.get(accountId, region, dateTime.toString("YYYY"), dateTime.toString("MM"),
                dateTime.toString("dd"), instanceId + "-" + dateTime).toString();

        switch (LogType.valueOf(logType)) {
            case USER_DATA:
                fileName = TAUPAGE_YAML;
                break;
            case AUDIT_LOG:
                fileName = AUDIT_LOG_FILE_NAME + new DateTime(UTC) + LOG_GZ;
                break;
            default:
                logger.error("Wrong logType given: " + logType);
                break;
        }
        ObjectMetadata metadata = new ObjectMetadata();
        byte[] decodedLogData = Base64.decode(logData);
        metadata.setContentLength(decodedLogData.length);

        InputStream stream = new ByteArrayInputStream(decodedLogData);

        putObjectToS3(bucketName, fileName, keyName, metadata, stream);
    }

    public void putObjectToS3(String bucket, String fileName, String keyName, ObjectMetadata metadata, InputStream
            stream) {
        AmazonS3 s3client = new AmazonS3Client();
        try {
            logger.info("Uploading a new object to S3 from a file");


            s3client.putObject(new PutObjectRequest(bucket, Paths.get(keyName, fileName).toString(), stream, metadata));

        } catch (AmazonServiceException ase) {
            logger.error("Caught an AmazonServiceException, which " +
                    "means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            logger.error("Error Message:    " + ase.getMessage());
            logger.error("HTTP Status Code: " + ase.getStatusCode());
            logger.error("AWS Error Code:   " + ase.getErrorCode());
            logger.error("Error Type:       " + ase.getErrorType());
            logger.error("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            logger.error("Caught an AmazonClientException, which " +
                    "means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            logger.error("Error Message: " + ace.getMessage());
        }
    }
}
