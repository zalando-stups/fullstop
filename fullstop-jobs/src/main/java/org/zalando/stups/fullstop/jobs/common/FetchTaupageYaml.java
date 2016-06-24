package org.zalando.stups.fullstop.jobs.common;

import org.zalando.stups.fullstop.taupage.TaupageYaml;

import java.util.Optional;

public interface FetchTaupageYaml {

    Optional<TaupageYaml> getTaupageYaml(String instanceId, String account, String region);

}
