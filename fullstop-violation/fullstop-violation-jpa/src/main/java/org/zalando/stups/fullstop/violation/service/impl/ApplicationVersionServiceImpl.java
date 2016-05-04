package org.zalando.stups.fullstop.violation.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.Stack;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.repository.ApplicationRepository;
import org.zalando.stups.fullstop.violation.repository.VersionRepository;
import org.zalando.stups.fullstop.violation.service.ApplicationVersionService;

import javax.transaction.Transactional;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

@Service
public class ApplicationVersionServiceImpl implements ApplicationVersionService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private VersionRepository versionRepository;

    @Override
    @Transactional(REQUIRES_NEW)
    public Stack saveStack(final String applicationId, final String applicationVersion) {

        if (applicationId == null && applicationVersion == null) {
            return new Stack( null, null );
        }

        ApplicationEntity application = null;
        VersionEntity version = null;

        if (applicationVersion != null) {
            version = versionRepository.findByName(applicationVersion);
            if (version == null) {
                version = versionRepository.save(new VersionEntity(applicationVersion));
            }
        }

        if (applicationId != null) {
            application = applicationRepository.findByName(applicationId);

            if (application == null) {
                application = new ApplicationEntity(applicationId);
            }

            if (version != null) {
                if (!application.getVersionEntities().contains(version)) {
                    application.getVersionEntities().add(version);
                }

            }
            application = applicationRepository.save(application);
        }

        return new Stack(application, version);
    }
}
