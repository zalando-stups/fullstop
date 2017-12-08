package org.zalando.stups.fullstop.plugin.lambda;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;
import org.zalando.stups.fullstop.plugin.lambda.config.LambdaPluginProperties;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Optional.empty;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.violationFor;
import static org.zalando.stups.fullstop.violation.ViolationType.LAMBDA_FUNCTION_CREATED_FROM_UNTRUSTED_LOCATION;

@Component
public class LambdaPlugin extends AbstractFullstopPlugin {

    private static final String EVENT_SOURCE = "lambda.amazonaws.com";

    private static final Pattern CREATE_FUNCTION_EVENT_REGEXP = Pattern.compile("CreateFunction.*");

    private static final Pattern UPDATE_FUNCTION_EVENT_REGEXP = Pattern.compile("UpdateFunctionCode.*");

    private static final String S3_BUCKET_CODE_JSON_PATH = "$.code.s3Bucket";
    private static final String S3_BUCKET_JSON_PATH = "$.s3Bucket";
    private static final String S3_KEY_CODE_JSON_PATH = "$.code.s3Key";
    private static final String S3_KEY_JSON_PATH = "$.s3Key";
    private static final String FUNCTION_NAME_JSON_PATH = "$.functionName";
    private static final String CODE_SHA_256_JSON_PATH = "$.codeSha256";

    private static final String S3_BUCKET = "s3_bucket";
    private static final String S3_KEY = "s3_key";
    private static final String FUNCTION_NAME = "function_name";

    private static final String EMPTY = "";

    private Set<String> whitelistedSha256 = Sets.newHashSet(
            "cax1BLJW1BIh8CUYWffXav7NJKdoWCh5J3AtW+MUbjs=", // Node.js 6.10
            "uOcL4KIfjdER8JrY4HgSFhPKbAvRmnpTTiT1VBiU0iA=", // C#
            "cl0KxraOjIeRrk9/rG4V1YZWZBi7eb2FvkQbTb396F4=", // Python 2.7
            "l8Gp1yL9P7AnffmihD3RXRqpo5xRl8tLfPW32v1soW4=", // Java 8
            "bRdSs7GKxebaTz/CmYYgQF55qOe+NWwSd1ylrYFAiaQ=", // Python 3.6
            "2XOUFxixUm6xRXd2zpy6649g5C34WBxLUrfu6TptJAs="  // Node.js 4.3
    );

    private final ViolationSink violationSink;

    private final LambdaPluginProperties lambdaPluginProperties;

    @Autowired
    public LambdaPlugin(final ViolationSink violationSink,
                        final LambdaPluginProperties lambdaPluginProperties) {
        this.violationSink = violationSink;
        this.lambdaPluginProperties = lambdaPluginProperties;
    }

    @Override
    public boolean supports(final CloudTrailEvent cloudTrailEvent) {
        return cloudTrailEvent.getEventData().getEventSource().equals(EVENT_SOURCE)
                && (isCreateFunctionEvent(cloudTrailEvent.getEventData().getEventName())
                || isUpdateFunctionEvent(cloudTrailEvent.getEventData().getEventName())
        );
    }


    @Override
    public void processEvent(final CloudTrailEvent event) {

        final String requestParameters = event.getEventData().getRequestParameters();
        final String responseElements = event.getEventData().getResponseElements();

        final Optional<String> s3Bucket = getFromJSON(requestParameters,
                S3_BUCKET_CODE_JSON_PATH,
                S3_BUCKET_JSON_PATH);

        if (!s3Bucket.isPresent() || !lambdaPluginProperties.getS3Buckets().contains(s3Bucket.get())) {

            // Workaround to avoid creating violation when AWS-console is used
            if (!isDefaultCodeFromAWS(getFromJSON(responseElements, CODE_SHA_256_JSON_PATH).orElse(EMPTY))) {
                violationSink.put(
                        violationFor(event)
                                .withPluginFullyQualifiedClassName(LambdaPlugin.class)
                                .withType(LAMBDA_FUNCTION_CREATED_FROM_UNTRUSTED_LOCATION)
                                .withMetaInfo(ImmutableMap
                                        .builder()
                                        .put(FUNCTION_NAME, getFromJSON(responseElements,
                                                FUNCTION_NAME_JSON_PATH)
                                                .orElse(EMPTY))
                                        .put(S3_BUCKET, s3Bucket.orElse(EMPTY))
                                        .put(S3_KEY, getFromJSON(requestParameters, S3_KEY_CODE_JSON_PATH,
                                                S3_KEY_JSON_PATH)
                                                .orElse(EMPTY))
                                        .build())
                                .build());
            }
        }
    }

    private boolean isDefaultCodeFromAWS(final String codeSha256) {
        return whitelistedSha256.contains(codeSha256);
    }

    private Optional<String> getFromJSON(final String json, final String... jsonPaths) {
        Optional<String> result = empty();

        for (final String jsonPath : jsonPaths) {
            try {
                result = Optional.ofNullable(JsonPath.read(json, jsonPath));
                if (result.isPresent()) {
                    return result;
                }
            } catch (final JsonPathException ignored) {
                // DO NOTHING
            }
        }
        return result;
    }

    private static boolean isUpdateFunctionEvent(final String eventName) {
        return UPDATE_FUNCTION_EVENT_REGEXP.matcher(eventName).matches();
    }

    private static boolean isCreateFunctionEvent(final String eventName) {
        return CREATE_FUNCTION_EVENT_REGEXP.matcher(eventName).matches();
    }


}
