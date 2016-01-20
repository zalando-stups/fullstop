package org.zalando.stups.fullstop.plugin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.plugin.impl.EC2InstanceContextProviderImpl;
import org.zalando.stups.fullstop.plugin.provider.*;
import org.zalando.stups.fullstop.plugin.provider.impl.*;
import org.zalando.stups.pierone.client.PieroneOperations;

import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Configuration
public class EC2InstanceContextConfig {

    @Autowired
    private KioOperations kioOperations;

    @Autowired
    private Function<String, PieroneOperations> pieroneOperationsProvider;

    @ConditionalOnMissingBean
    @Bean
    EC2InstanceContextProvider contextProvider(ClientProvider clientProvider,
                                               @Value("${fullstop.plugins.ami.taupageNamePrefix}") final String taupageNamePrefix,
                                               @Value("${fullstop.plugins.ami.taupageOwners}") final String taupageOwners) {
        return new EC2InstanceContextProviderImpl(
                clientProvider,
                amiIdProvider(),
                amiProvider(),
                taupageYamlProvider(),
                taupageNamePrefix,
                Stream.of(taupageOwners.split(",")).filter(s -> !s.isEmpty()).collect(toList()),
                kioApplicationProvider(),
                kioVersionProvider(),
                kioApprovalProvider(),
                pieroneTagProvider(),
                scmSourceProvider());
    }

    @Bean
    AmiIdProvider amiIdProvider() {
        return new AmiIdProviderImpl();
    }

    @Bean
    AmiProvider amiProvider() {
        return new AmiProviderImpl();
    }

    @Bean
    TaupageYamlProvider taupageYamlProvider() {
        return new TaupageYamlProviderImpl();
    }

    @Bean
    KioApplicationProvider kioApplicationProvider() {
        return new KioApplicationProviderImpl(kioOperations);
    }

    @Bean
    KioVersionProvider kioVersionProvider() {
        return new KioVersionProviderImpl(kioOperations);
    }

    @Bean
    KioApprovalProvider kioApprovalProvider() {
        return new KioApprovalProviderImpl(kioOperations);
    }

    @Bean
    PieroneTagProvider pieroneTagProvider() {
        return new PieroneTagProviderImpl(pieroneOperationsProvider);
    }

    @Bean
    ScmSourceProvider scmSourceProvider() {
        return new ScmSourceProviderImpl(pieroneOperationsProvider);
    }
}
