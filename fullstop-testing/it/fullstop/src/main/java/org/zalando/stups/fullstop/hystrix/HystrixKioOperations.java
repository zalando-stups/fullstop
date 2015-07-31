/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.hystrix;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.zalando.stups.clients.kio.*;

import java.util.List;

public class HystrixKioOperations implements KioOperations {

    private final KioOperations delegate;

    public HystrixKioOperations(final KioOperations delegate) {
        this.delegate = delegate;
    }

    @Override
    @HystrixCommand
    public List<ApplicationBase> listApplications() {
        return delegate.listApplications();
    }

    @Override
    @HystrixCommand
    public Application getApplicationById(final String applicationId) {
        return delegate.getApplicationById(applicationId);
    }

    @Override
    @HystrixCommand
    public void createOrUpdateApplication(final CreateOrUpdateApplicationRequest request, final String applicationId) {
        delegate.createOrUpdateApplication(request, applicationId);
    }

    @Override
    @HystrixCommand
    public List<String> getApplicationApprovals(final String applicationId) {
        return delegate.getApplicationApprovals(applicationId);
    }

    @Override
    @HystrixCommand
    public List<VersionBase> getApplicationVersions(final String applicationId) {
        return delegate.getApplicationVersions(applicationId);
    }

    @Override
    @HystrixCommand
    public Version getApplicationVersion(final String applicationId, final String versionId) {
        return delegate.getApplicationVersion(applicationId, versionId);
    }

    @Override
    @HystrixCommand
    public void createOrUpdateVersion(final CreateOrUpdateVersionRequest request, final String applicationId,
            final String versionId) {
        delegate.createOrUpdateVersion(request, applicationId, versionId);
    }

    @Override
    @HystrixCommand
    public List<Approval> getApplicationApprovals(final String applicationId, final String versionId) {
        return delegate.getApplicationApprovals(applicationId, versionId);
    }

    @Override
    @HystrixCommand
    public void approveApplicationVersion(final ApprovalBase request, final String applicationId,
            final String versionId) {
        delegate.approveApplicationVersion(request, applicationId, versionId);
    }
}
