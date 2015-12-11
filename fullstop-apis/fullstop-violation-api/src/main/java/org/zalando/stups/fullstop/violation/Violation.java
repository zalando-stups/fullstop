package org.zalando.stups.fullstop.violation;

/**
 * @author mrandi
 */

public interface Violation {

    String getEventId();

    String getAccountId();

    String getRegion();

    String getComment();

    Object getMetaInfo();

    String getPluginFullyQualifiedClassName();

    String getViolationType();

    String getUsername();

    Boolean getChecked();

    String getInstanceId();
}
