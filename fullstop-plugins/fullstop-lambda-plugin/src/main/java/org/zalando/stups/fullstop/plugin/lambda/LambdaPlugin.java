package org.zalando.stups.fullstop.plugin.lambda;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.google.common.collect.ImmutableMap;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;
import org.zalando.stups.fullstop.plugin.lambda.config.LambdaPluginProperties;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Optional.empty;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.violationFor;
import static org.zalando.stups.fullstop.violation.ViolationType.LAMBDA_FUNCTION_CREATED_FROM_UNTRUSTED_LOCATION;

@Component
public class LambdaPlugin extends AbstractFullstopPlugin {

    private static final String EVENT_SOURCE = "lambda.amazonaws.com";

    private static final Pattern CREATE_FUNCTION_EVENT_REGEXP = Pattern.compile("CreateFunction.*");

    private static final Pattern UPDATE_FUNCTION_EVENT_REGEXP = Pattern.compile("UpdateFunctionCode.*");

    private static final String S3_BUCKET_JSON_PATH = "$.code.s3Bucket";
    private static final String S3_KEY_JSON_PATH = "$.code.s3Key";
    private static final String S3_BUCKET = "s3-bucket";
    private static final String S3_KEY = "s3-key";
    private static final String EMPTY = "";

    private final ViolationSink violationSink;

    private final LambdaPluginProperties lambdaPluginProperties;

    @Autowired
    public LambdaPlugin(final ViolationSink violationSink,
                        final LambdaPluginProperties lambdaPluginProperties) {
        this.violationSink = violationSink;
        this.lambdaPluginProperties = lambdaPluginProperties;
    }

    @Override
    public boolean supports(final CloudTrailEvent delimiter) {
        return delimiter.getEventData().getEventSource().equals(EVENT_SOURCE)
                && (
                isCreateFunctionEvent(delimiter.getEventData().getEventName())
                        || isUpdateFunctionEvent(delimiter.getEventData().getEventName())
        );
    }


    @Override
    public void processEvent(final CloudTrailEvent event) {

        final String parameters = event.getEventData().getRequestParameters();

        final Optional<String> s3Bucket = getS3Bucket(parameters);

        if (!s3Bucket.isPresent() || !lambdaPluginProperties.getS3Buckets().contains(s3Bucket.get())) {
            violationSink.put(
                    violationFor(event)
                            .withPluginFullyQualifiedClassName(LambdaPlugin.class)
                            .withType(LAMBDA_FUNCTION_CREATED_FROM_UNTRUSTED_LOCATION)
                            .withMetaInfo(ImmutableMap
                                    .builder()
                                    .put(S3_BUCKET, s3Bucket.orElse(EMPTY))
                                    .put(S3_KEY, getS3BucketKey(parameters).orElse(EMPTY))
                                    .build())
                            .build());
        }
    }

    private Optional<String> getS3Bucket(final String parameters) {
        try {
            return Optional.ofNullable(JsonPath.read(parameters, S3_BUCKET_JSON_PATH));
        } catch (final JsonPathException ignored) {
            return empty();
        }
    }

    private Optional<String> getS3BucketKey(final String parameters) {
        try {
            return Optional.ofNullable(JsonPath.read(parameters, S3_KEY_JSON_PATH));
        } catch (final JsonPathException ignored) {
            return empty();
        }
    }

    private static boolean isUpdateFunctionEvent(final String eventName) {
        return UPDATE_FUNCTION_EVENT_REGEXP.matcher(eventName).matches();
    }

    private static boolean isCreateFunctionEvent(final String eventName) {
        return CREATE_FUNCTION_EVENT_REGEXP.matcher(eventName).matches();
    }


}
