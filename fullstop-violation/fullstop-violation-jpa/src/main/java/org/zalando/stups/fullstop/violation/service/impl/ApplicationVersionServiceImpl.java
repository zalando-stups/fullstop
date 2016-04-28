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
        VersionEntity version = versionRepository.findByName(applicationId);
        if (version == null) {
            version =  versionRepository.save(new VersionEntity(applicationVersion));
        }

        ApplicationEntity application = applicationRepository.findByName(applicationId);
        if (application == null) {
            application = new ApplicationEntity(applicationId);
        }

        if (!application.getVersionEntities().contains(version)) {
            application.getVersionEntities().add(version);
        }

        application = applicationRepository.save(application);

        return new Stack(application, version);
    }
}
