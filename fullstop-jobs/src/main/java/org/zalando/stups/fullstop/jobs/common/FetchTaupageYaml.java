package org.zalando.stups.fullstop.jobs.common;

import java.util.Map;
import java.util.Optional;

public interface FetchTaupageYaml {

    Optional<Map> getTaupageYaml (String instanceId, String account, String region);

}
