package org.zalando.stups.fullstop.plugin.lambda;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;

import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Optional.empty;

final class LambdaEventUtil {

    private static final String LAMBDA_SOURCE = "lambda.amazonaws.com";
    private static final Pattern CREATE_FUNCTION_EVENT_REGEXP = Pattern.compile("CreateFunction.*");
    private static final Pattern UPDATE_FUNCTION_EVENT_REGEXP = Pattern.compile("UpdateFunctionCode.*");
    private static final String FUNCTION_NAME_JSON_PATH = "$.functionName";
    private static final String FUNCTION_ARN_JSON_PATH = "$.functionArn";
    static final String EMPTY = "";
    static final String FUNCTION_NAME = "function_name";

    private LambdaEventUtil() {
    }

    static boolean hasLambdaSource(CloudTrailEvent event) {
        return event.getEventData().getEventSource().equals(LAMBDA_SOURCE);
    }

    static boolean isUpdateFunctionEvent(CloudTrailEvent event) {
        return UPDATE_FUNCTION_EVENT_REGEXP.matcher(event.getEventData().getEventName()).matches();
    }

    static boolean isCreateFunctionEvent(CloudTrailEvent event) {
        return CREATE_FUNCTION_EVENT_REGEXP.matcher(event.getEventData().getEventName()).matches();
    }

    static Optional<String> getFromJSON(final String json, final String... jsonPaths) {
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

    static Optional<String> getFunctionName(CloudTrailEvent event) {
        return getFromJSON(event.getEventData().getResponseElements(), FUNCTION_NAME_JSON_PATH);
    }

    static Optional<String> getFunctionArn(CloudTrailEvent event) {
        return getFromJSON(event.getEventData().getResponseElements(), FUNCTION_ARN_JSON_PATH);
    }
}
