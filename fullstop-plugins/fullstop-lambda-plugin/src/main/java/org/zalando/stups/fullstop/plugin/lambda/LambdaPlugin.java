package org.zalando.stups.fullstop.plugin.lambda;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;

import java.util.regex.Pattern;

/**
 * Just for testing.
 *
 * @author jbellmann
 */
@Component
public class LambdaPlugin extends AbstractFullstopPlugin {

    private static final String EVENT_SOURCE = "lambda.amazonaws.com";

    @Autowired
    private final LambdaScmSourceValidator scmSourceValidator;

    @Autowired
    private final LambdaPermissionValidator permissionValidator;

    private static final Pattern CREATE_FUNCTION_EVENT_REGEXP = Pattern.compile("CreateFunction.*");

    private static final Pattern UPDATE_FUNCTION_EVENT_REGEXP = Pattern.compile("UpdateFunctionCode.*");

    private static final Pattern PERMISSION_EVENT_REGEXP = Pattern.compile("AddPermission.*");


    @Autowired
    public LambdaPlugin(LambdaScmSourceValidator scmSourceValidator, LambdaPermissionValidator permissionValidator){
        this.scmSourceValidator = scmSourceValidator;
        this.permissionValidator = permissionValidator;
    }

    @Override
    public boolean supports(final CloudTrailEvent delimiter) {
        return delimiter.getEventData().getEventSource().equals(EVENT_SOURCE)
                && (
                isCreateFuncitonEvent(delimiter.getEventData().getEventName())
                || isUpdateFuncitonEvent(delimiter.getEventData().getEventName())
        );
    }

    static boolean isCreateFuncitonEvent(final String eventName){
        return CREATE_FUNCTION_EVENT_REGEXP.matcher(eventName).matches();
    }

    static boolean isUpdateFuncitonEvent(final String eventName){
        return UPDATE_FUNCTION_EVENT_REGEXP.matcher(eventName).matches();
    }

    static boolean isPermissionEvent(final String eventName){
        return PERMISSION_EVENT_REGEXP.matcher(eventName).matches();
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {
        final String eventName = event.getEventData().getEventName();

        if(isCreateFuncitonEvent(eventName) || isUpdateFuncitonEvent(eventName)){
            scmSourceValidator.processEvent(event);
        } else if(isPermissionEvent(eventName)) {
            permissionValidator.validateEvent(event);
        }
    }



}
