package org.zalando.stups.fullstop.plugin.provider;


import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.taupage.TaupageYaml;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface TaupageYamlProvider extends Function<EC2InstanceContext, Optional<TaupageYaml>> {
}
