package org.zalando.stups.fullstop.plugin.lambda;

import com.amazonaws.regions.Region;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.FunctionCodeLocation;
import com.amazonaws.services.lambda.model.GetFunctionRequest;
import com.amazonaws.services.lambda.model.GetFunctionResult;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.CloudTrailEventSupport;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.amazonaws.regions.Region.getRegion;
import static com.amazonaws.regions.Regions.fromName;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.violationFor;


@Component
public class LambdaScmSourceValidator {

    private final String scmSourceBucketName;

    private final String lambdaBucketName;

    private final ClientProvider clientProvider;


    private static final Logger LOG = LoggerFactory.getLogger(LambdaScmSourceValidator.class);

    private static final String FUNCTION_ARN_JSON_PATH = "$.functionArn";

    private static final String FUNCTION_VERSION_JSON_PATH = "$.version";

    private static final String KEY_SEPARATOR = "-";

    private final AmazonS3Client s3Client = new AmazonS3Client();

    private static final String SCM_JSON = "scm-source.json";

    private static final String TMP_FILE_SUFFIX = "tmp";

    //// TODO: 8/16/16 Add violation type
    private static final String VIOLATION_TYPE = "no scm-source file violation" ;

    private final ViolationSink violationSink;


    public static class Result {
        public Result(PutObjectResult scmResult, PutObjectResult lambdaFileResult) {
            this.scmResult = scmResult;
            this.lambdaFileResult = lambdaFileResult;
        }
        PutObjectResult scmResult;
        PutObjectResult lambdaFileResult;
    }


    @Autowired
    public LambdaScmSourceValidator(String scmSourceBucketName, String lambdaBucketName, ClientProvider clientProvider, ViolationSink violationSink) {
        this.scmSourceBucketName = scmSourceBucketName;
        this.lambdaBucketName = lambdaBucketName;
        this.clientProvider = clientProvider;
        this.violationSink = violationSink;
    }

    public Optional<Result> processEvent(final CloudTrailEvent event)  {
        String elems = event.getEventData().getResponseElements();
        List<String> functionArns = CloudTrailEventSupport.read(elems, FUNCTION_ARN_JSON_PATH);
        String functionArn = functionArns.get(0);
        Region region = getRegion(fromName(event.getEventData().getAwsRegion()));
        String account = event.getEventData().getAccountId();
        String version = CloudTrailEventSupport.read(elems, FUNCTION_VERSION_JSON_PATH).get(0);
        String eventId = event.getEventData().getEventId().toString();

        String functionLocation = getLambdaFunctionLocation(functionArn, version, region, account);
        File lambdaFile;
        try {
            lambdaFile = getLambdaFile(functionLocation, functionArn);
        } catch (IOException e) {
            LOG.error("Error while downloading Lambda File of Function ARN " + functionArn, e);
            return Optional.empty();
        }


        Optional<File> scmSourceJsonFileOpt;
        try {
            scmSourceJsonFileOpt = extractScmSourceJson(lambdaFile);
        } catch (IOException e) {
            LOG.error("Error while extracting Lambda File of Function ARN " + functionArn, e);
            return Optional.empty();
        }

        if(!scmSourceJsonFileOpt.isPresent()){
            LOG.debug("scm-source.json file not found for event " + event.toString());
            Violation vio = violationFor(event).withType(VIOLATION_TYPE).build();
            violationSink.put(vio);
            return Optional.empty();
        }

        PutObjectResult r1 = s3Client.putObject(scmSourceBucketName, createKey(functionArn, version, eventId), scmSourceJsonFileOpt.get());
        PutObjectResult r2 = s3Client.putObject(lambdaBucketName, createKey(functionArn, version, eventId), lambdaFile);
        return Optional.of(new Result(r1, r2));
    }

    String getLambdaFunctionLocation(final String arn, final String version, final Region region, final String accountId) {
        AWSLambdaClient client = clientProvider.getClient(AWSLambdaClient.class, accountId, region);
        GetFunctionRequest request = new GetFunctionRequest().withFunctionName(arn).withQualifier(version);
        GetFunctionResult result = client.getFunction(request);
        FunctionCodeLocation location = result.getCode();
        return location.getLocation();
    }

    File getLambdaFile(final String s3Url, final String functionArn) throws IOException {
        File f = File.createTempFile(functionArn, TMP_FILE_SUFFIX);
        URL url = new URL(s3Url);
        FileUtils.copyURLToFile(url, f);
        return f;
    }

    static Optional<File> extractScmSourceJson(final File lambdaFile) throws IOException {
        InputStream inputStream = new FileInputStream(lambdaFile);
        File scmSourceFile  = null;
        FileOutputStream fileOutputStream = null ;
        try {
            ZipInputStream zis = new ZipInputStream(inputStream);
            ZipEntry entry ;

            entry = zis.getNextEntry();
            boolean isFound = false;
            byte[] buffer = new byte[2048];
            while (!isFound && entry != null) {
                String name = getFileName(entry);
                if (SCM_JSON.equals(name)) {
                    scmSourceFile = File.createTempFile(SCM_JSON, TMP_FILE_SUFFIX);
                    fileOutputStream = new FileOutputStream(scmSourceFile);
                    int len = 0;
                    len = zis.read(buffer);
                    while (len > 0) {
                        fileOutputStream.write(buffer, 0, len);
                        len = zis.read(buffer);
                    }
                    isFound = true;
                }
                entry = zis.getNextEntry();
            }
            if (isFound) {
                return Optional.of(scmSourceFile);
            } else {
                return Optional.empty();
            }
        }  finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
                inputStream.close();
            }
        }
    }

    private static String getFileName(final ZipEntry entry) {
        String[] array = entry.getName().split(File.separator);
        if(array.length >= 2){
            return array[1];
        } else {
            return null;
        }
    }

    static String createKey(final String functionArn, final String version, final String eventId){
        return functionArn + KEY_SEPARATOR + version + KEY_SEPARATOR + eventId;
    }
}
