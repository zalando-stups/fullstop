package org.zalando.stups.fullstop.plugin.unapproved.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.zalando.stups.fullstop.plugin.unapproved.PolicyTemplatesProvider;
import org.zalando.stups.fullstop.s3.S3Service;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created by mrandi.
 */
@Service
public class S3PolicyTemplatesProvider implements PolicyTemplatesProvider {

    @Value("${fullstop.plugins.unapprovedServicesAndRole.bucketName}")
    private String bucketName;

    @Value("${fullstop.plugins.unapprovedServicesAndRole.prefix}/")
    private String prefix;

    private LoadingCache<String, String> cache = null;

    private S3Service s3Service;

    private List<String> policyTemplateNames;

    @Autowired
    public S3PolicyTemplatesProvider(final S3Service s3Service) {
        this.s3Service = s3Service;
        init();
    }

    @Scheduled(initialDelay = 2 * 1000, fixedDelay = 60 * 60 * 1000)
    private void fetchFromS3() {
        List<String> results = newArrayList();
        List<String> listS3Objects = s3Service.listS3Objects(bucketName, prefix);

        for (String listS3Object : listS3Objects) {
            results.add(Files.getNameWithoutExtension(listS3Object));
        }

        policyTemplateNames = results;
    }

    @Override public List<String> getPolicyTemplateNames() {
        // when application starts, need 2 seconds to fetch the first entries
        if (policyTemplateNames == null || policyTemplateNames.isEmpty()) {
            fetchFromS3();
        }
        return policyTemplateNames;
    }

    @Override public String getPolicyTemplate(final String roleName) {
        return cache.getUnchecked(roleName);
    }

    private void init() {
        cache = CacheBuilder.newBuilder().maximumSize(10).expireAfterWrite(1, MINUTES).build(
                new CacheLoader<String, String>() {
                    private final Logger logger = LoggerFactory.getLogger(CacheLoader.class);

                    @Override
                    public String load(final String roleName) throws Exception {
                        logger.debug("CacheLoader active for role : {}", roleName);

                        String key = prefix + roleName + ".json";

                        String result = s3Service.downloadObject(bucketName, key);
                        if (result == null) {
                            throw new RuntimeException("Could not download key:" + key);
                        }
                        return result;
                    }
                });
    }

}
