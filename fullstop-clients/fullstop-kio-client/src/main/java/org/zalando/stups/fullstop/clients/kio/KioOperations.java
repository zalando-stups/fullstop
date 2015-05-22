package org.zalando.stups.fullstop.clients.kio;

import java.util.List;

public interface KioOperations {

    /**
     * Returns a list of registered applications in Kio.
     *
     * @return  {@link ApplicationBase} subset of {@link Application}.
     */
    List<ApplicationBase> listApplications();

    /**
     * Returns an application for the given applicationId.
     *
     * @param   applicationId
     *
     * @return  {@link Application}
     */
    Application getApplicationById(String applicationId);

    /**
     * Creates or updates an application.
     *
     * @param  request
     * @param  id
     */
    void createOrUpdateApplication(CreateOrUpdateApplicationRequest request, String applicationId);

    /**
     * Returns a list of all approval-names for the specified applicationId.
     *
     * @return
     */
    List<String> getApplicationApprovals(String applicationId);

    List<VersionBase> getApplicationVersions(String applicationId);

    Version getApplicationVersion(String applicationId, String versionId);

    void createOrUpdateVersion(CreateOrUpdateVersionRequest request, String applicationId, String versionId);

    List<Approval> getApplicationApprovals(String applicationId, String versionId);

    void approveApplicationVersion(ApprovalBase request, String applicationId, String versionId);

}
