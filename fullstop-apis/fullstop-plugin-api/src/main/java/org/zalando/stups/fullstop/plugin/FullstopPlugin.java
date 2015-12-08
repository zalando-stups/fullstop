package org.zalando.stups.fullstop.plugin;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.springframework.plugin.core.Plugin;
import org.springframework.plugin.metadata.MetadataProvider;

/**
 * An {@link FullstopPlugin} just processes an {@link CloudTrailEvent}.
 *
 * @author jbellmann
 */
public interface FullstopPlugin extends Plugin<CloudTrailEvent>, MetadataProvider {

    void processEvent(CloudTrailEvent event);

}
