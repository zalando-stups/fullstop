package org.zalando.stups.fullstop.plugin.lambda;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;
import org.zalando.stups.fullstop.plugin.lambda.config.LambdaPluginProperties;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Optional;

import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.violationFor;
import static org.zalando.stups.fullstop.plugin.lambda.LambdaEventUtil.EMPTY;
import static org.zalando.stups.fullstop.plugin.lambda.LambdaEventUtil.FUNCTION_NAME;
import static org.zalando.stups.fullstop.plugin.lambda.LambdaEventUtil.getFromJSON;
import static org.zalando.stups.fullstop.plugin.lambda.LambdaEventUtil.getFunctionName;
import static org.zalando.stups.fullstop.plugin.lambda.LambdaEventUtil.hasLambdaSource;
import static org.zalando.stups.fullstop.plugin.lambda.LambdaEventUtil.isCreateFunctionEvent;
import static org.zalando.stups.fullstop.plugin.lambda.LambdaEventUtil.isUpdateFunctionEvent;
import static org.zalando.stups.fullstop.violation.ViolationType.LAMBDA_FUNCTION_CREATED_FROM_UNTRUSTED_LOCATION;

@Component
public class LambdaPlugin extends AbstractFullstopPlugin {

    private static final String S3_BUCKET_CODE_JSON_PATH = "$.code.s3Bucket";
    private static final String S3_BUCKET_JSON_PATH = "$.s3Bucket";
    private static final String S3_KEY_CODE_JSON_PATH = "$.code.s3Key";
    private static final String S3_KEY_JSON_PATH = "$.s3Key";

    private static final String S3_BUCKET = "s3_bucket";
    private static final String S3_KEY = "s3_key";

    private final ViolationSink violationSink;

    private final LambdaPluginProperties lambdaPluginProperties;

    @Autowired
    public LambdaPlugin(final ViolationSink violationSink,
                        final LambdaPluginProperties lambdaPluginProperties) {
        this.violationSink = violationSink;
        this.lambdaPluginProperties = lambdaPluginProperties;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        return hasLambdaSource(event) && (isCreateFunctionEvent(event) || isUpdateFunctionEvent(event));
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {
        final String requestParameters = event.getEventData().getRequestParameters();
        final Optional<String> s3Bucket = getFromJSON(requestParameters,
                S3_BUCKET_CODE_JSON_PATH,
                S3_BUCKET_JSON_PATH);

        if (!s3Bucket.isPresent() || !lambdaPluginProperties.getS3Buckets().contains(s3Bucket.get())) {
            violationSink.put(
                    violationFor(event)
                            .withPluginFullyQualifiedClassName(LambdaPlugin.class)
                            .withType(LAMBDA_FUNCTION_CREATED_FROM_UNTRUSTED_LOCATION)
                            .withMetaInfo(ImmutableMap
                                    .builder()
                                    .put(FUNCTION_NAME, getFunctionName(event).orElse(EMPTY))
                                    .put(S3_BUCKET, s3Bucket.orElse(EMPTY))
                                    .put(S3_KEY, getFromJSON(requestParameters, S3_KEY_CODE_JSON_PATH,
                                            S3_KEY_JSON_PATH)
                                            .orElse(EMPTY))
                                    .build())
                            .build());
        }
    }

}
