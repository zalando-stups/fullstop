package org.zalando.stups.fullstop.whitelist;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import org.slf4j.Logger;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;
import static org.slf4j.LoggerFactory.getLogger;

public class WhitelistRulesEvaluator implements BiFunction<RuleEntity, ViolationEntity, Boolean> {

    private final Logger log = getLogger(getClass());
    private final ObjectMapper om = new ObjectMapper();

    /**
     * true if rule matches a violation and should be whitelisted.
     *
     * @return true if rule matches a violation
     */
    @Override
    public Boolean apply(final RuleEntity ruleEntity, final ViolationEntity violationEntity) {
        final List<Predicate<ViolationEntity>> predicates = newArrayList();


        trimOptional(ruleEntity.getAccountId())
                .map(WhitelistRulesEvaluator::accountIsEqual)
                .ifPresent(predicates::add);

        trimOptional(ruleEntity.getRegion())
                .map(WhitelistRulesEvaluator::regionIsEqual)
                .ifPresent(predicates::add);

        trimOptional(ruleEntity.getViolationTypeEntityId())
                .map(WhitelistRulesEvaluator::violationTypeIdIsEqual)
                .ifPresent(predicates::add);

        trimOptional(ruleEntity.getImageName())
                .map(WhitelistRulesEvaluator::imageNameMatches)
                .ifPresent(predicates::add);

        trimOptional(ruleEntity.getImageOwner())
                .map(WhitelistRulesEvaluator::imageOwnerIsEqual)
                .ifPresent(predicates::add);

        trimOptional(ruleEntity.getApplicationId())
                .map(WhitelistRulesEvaluator::applicationIdIsEqual)
                .ifPresent(predicates::add);

        trimOptional(ruleEntity.getApplicationVersion())
                .map(WhitelistRulesEvaluator::applicationVersionIsEqual)
                .ifPresent(predicates::add);

        trimOptional(ruleEntity.getMetaInfoJsonPath())
                .map(this::metaInfoJsonPathExists)
                .ifPresent(predicates::add);

        final Optional<Predicate<ViolationEntity>> whiteListTest = predicates.stream().reduce(Predicate::and);

        return whiteListTest.isPresent() && whiteListTest.get().test(violationEntity);
    }

    private static Predicate<ViolationEntity> accountIsEqual(final String account) {
        return v -> account.equals(v.getAccountId());
    }

    private static Predicate<ViolationEntity> regionIsEqual(final String region) {
        return v -> region.equals(v.getRegion());
    }

    private static Predicate<ViolationEntity> violationTypeIdIsEqual(final String violationTypeId) {
        return v -> violationTypeId.equals(v.getViolationTypeEntity().getId());
    }

    private static Predicate<ViolationEntity> imageNameMatches(final String imageNamePattern) {
        return v -> {
            if(v.getMetaInfo() instanceof Map) {
                final Map<String,String> map = (Map<String, String>) v.getMetaInfo();
                if (map == null || map.get("ami_name") == null) {
                    return false;
                }
                return map.get("ami_name").matches(imageNamePattern);
            } else {
                return false;
            }
        };
    }

    private static Predicate<ViolationEntity> imageOwnerIsEqual(final String imageOwner) {
        return v -> {
            if(v.getMetaInfo() instanceof Map) {
                final Map<String,String> map = (Map<String, String>) v.getMetaInfo();
                return imageOwner.equals(map.get("ami_owner_id"));
            } else {
                return false;
            }
        };
    }

    private static Predicate<ViolationEntity> applicationIdIsEqual(final String applicationId) {
        return v -> applicationId.equals(
                Optional.ofNullable(
                        v.getApplication()).
                        map(ApplicationEntity::getName).
                        orElse(null));
    }

    private static Predicate<ViolationEntity> applicationVersionIsEqual(final String applicationVersion) {
        return v -> applicationVersion.equals(
                Optional.ofNullable(
                        v.getApplicationVersion()).
                        map(VersionEntity::getName).
                        orElse(null));
    }

    private Predicate<ViolationEntity> metaInfoJsonPathExists(final String jsonPathDefinition) {
        return v -> {
            final JsonPath jsonPath = JsonPath.compile(jsonPathDefinition);
            final String json;
            try {
                json = om.writeValueAsString(v.getMetaInfo());
            } catch (JsonProcessingException e) {
                log.warn("Could not read violation metaInfo as JSON: " + v, e);
                return false;
            }
            try {
                final List<?> matches = jsonPath.read(json);
                return !matches.isEmpty();
            } catch (JsonPathException e) {
                return false;
            }
        };
    }

    private static Optional<String> trimOptional(final String value) {
        return Optional.ofNullable(value).map(String::trim).filter(string -> !string.isEmpty());
    }

}
