package org.zalando.stups.fullstop.whitelist;

import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;

public class WhitelistRulesEvaluator implements BiFunction<RuleEntity, ViolationEntity, Boolean> {


    /**
     * true if rule matches a violation and should be whitelisted.
     *
     * @return true if rule matches a violation
     */
    @Override
    public Boolean apply(RuleEntity ruleEntity, ViolationEntity violationEntity) {
        List<Predicate<ViolationEntity>> predicates = newArrayList();


        trimOptional(ruleEntity.getAccountId())
                .map(WhitelistRulesEvaluator::accountIsEqual)
                .ifPresent(predicates::add);

        trimOptional(ruleEntity.getRegion())
                .map(WhitelistRulesEvaluator::regionIsEqual)
                .ifPresent(predicates::add);

        trimOptional(ruleEntity.getViolationTypeId())
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

        Optional<Predicate<ViolationEntity>> whiteListTest = predicates.stream().reduce(Predicate::and);

        return whiteListTest.isPresent() && whiteListTest.get().test(violationEntity);
    }

    private static Predicate<ViolationEntity> accountIsEqual(String account) {
        return v -> account.equals(v.getAccountId());
    }

    private static Predicate<ViolationEntity> regionIsEqual(String region) {
        return v -> region.equals(v.getRegion());
    }

    private static Predicate<ViolationEntity> violationTypeIdIsEqual(String violationTypeId) {
        return v -> violationTypeId.equals(v.getViolationTypeEntity().getId());
    }

    private static Predicate<ViolationEntity> imageNameMatches(String imageNamePattern) {
        return v -> {
            if(v.getMetaInfo() instanceof Map) {
                Map<String,String> map = (Map<String, String>) v.getMetaInfo();
                return map.get("ami_name").matches(imageNamePattern);
            } else {
                return false;
            }
        };
    }

    private static Predicate<ViolationEntity> imageOwnerIsEqual(String imageOwner) {
        return v -> {
            if(v.getMetaInfo() instanceof Map) {
                Map<String,String> map = (Map<String, String>) v.getMetaInfo();
                return imageOwner.equals(map.get("ami_owner_id"));
            } else {
                return false;
            }
        };
    }

    private static Predicate<ViolationEntity> applicationIdIsEqual(String applicationId) {
        return v -> {
            if(v.getMetaInfo() instanceof Map) {
                Map<String,String> map = (Map<String, String>) v.getMetaInfo();
                return applicationId.equals(map.get("application_id"));
            } else {
                return false;
            }
        };
    }

    private static Predicate<ViolationEntity> applicationVersionIsEqual(String applicationVersion) {
        return v -> {
            if(v.getMetaInfo() instanceof Map) {
                Map<String,String> map = (Map<String, String>) v.getMetaInfo();
                return applicationVersion.equals(map.get("application_version"));
            } else {
                return false;
            }
        };
    }


    private static Optional<String> trimOptional(String value) {
        return Optional.ofNullable(value).map(String::trim).filter(string -> !string.isEmpty());
    }

}
