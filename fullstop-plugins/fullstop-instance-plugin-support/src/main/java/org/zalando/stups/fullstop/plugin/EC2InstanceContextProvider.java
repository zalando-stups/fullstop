package org.zalando.stups.fullstop.plugin;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;

import java.util.List;

public interface EC2InstanceContextProvider {

    List<EC2InstanceContext> instancesIn(CloudTrailEvent event);
}
