package org.zalando.stups.fullstop.whitelist;

import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.assertj.core.api.Assertions.assertThat;

public class WhitelistRulesEvaluatorTest {

    ViolationEntity violationEntity;
    RuleEntity ruleEntity;
    WhitelistRulesEvaluator evaluator;

    @Before
    public void setUp() throws Exception {
        ruleEntity = new RuleEntity();
        evaluator = new WhitelistRulesEvaluator();

    }


    @Test
    public void testAccoundID() throws Exception {
        violationEntity = new ViolationEntity(null, "1234", null, null, null, null, null, null, null, null);
        ruleEntity.setAccountId("1234");
        Boolean apply = evaluator.apply(ruleEntity, violationEntity);
        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testRegion() throws Exception {
        violationEntity = new ViolationEntity(null, null, "eu-west-1", null, null, null, null, null, null, null);
        ruleEntity.setRegion("eu-west-1");

        Boolean apply = evaluator.apply(ruleEntity, violationEntity);
        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testViolationType() throws Exception {
        violationEntity = new ViolationEntity(null, null, null, null, null, null, null, null, null, null);
        violationEntity.setViolationTypeEntity(new ViolationTypeEntity("NOT_FOUND_IN_KIO"));
        ruleEntity.setViolationTypeEntityId("NOT_FOUND_IN_KIO");

        Boolean apply = evaluator.apply(ruleEntity, violationEntity);
        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testImageOwner() throws Exception {
        Map<String, String> metainfo = newHashMap();
        metainfo.put("ami_owner_id", "Team");
        violationEntity = new ViolationEntity(null, null, null, null, metainfo, null, null, null, null, null);
        ruleEntity.setImageOwner("Team");

        Boolean apply = evaluator.apply(ruleEntity, violationEntity);
        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testApplicationID() throws Exception {
        Map<String, String> metainfo = newHashMap();
        metainfo.put("application_id", "myApp");
        violationEntity = new ViolationEntity(null, null, null, null, metainfo, null, null, null, null, null);
        ruleEntity.setApplicationId("myApp");

        Boolean apply = evaluator.apply(ruleEntity, violationEntity);
        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testApplicationVersion() throws Exception {
        Map<String, String> metainfo = newHashMap();
        metainfo.put("application_version", "1.0-Snapshot");
        violationEntity = new ViolationEntity(null, null, null, null, metainfo, null, null, null, null, null);
        ruleEntity.setApplicationVersion("1.0-Snapshot");

        Boolean apply = evaluator.apply(ruleEntity, violationEntity);
        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testNullMetaInfo() throws Exception {
        Map<String, String> metainfo = newHashMap();
        violationEntity = new ViolationEntity(null, null, null, null, metainfo, null, null, null, null, null);
        ruleEntity.setApplicationVersion("1.0-Snapshot");

        Boolean apply = evaluator.apply(ruleEntity, violationEntity);
        assertThat(apply).isEqualTo(false);
    }

    @Test
    public void testRegexStart() throws Exception{
        Map<String, String> metainfo = newHashMap();
        metainfo.put("ami_name", "CD-jenkins");
        violationEntity = new ViolationEntity(null, null, null, null, metainfo, null, null, null, null, null);
        ruleEntity.setImageName(".+jenkins");
        Boolean apply = evaluator.apply(ruleEntity, violationEntity);

        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testRegexMiddle() throws Exception{
        Map<String, String> metainfo = newHashMap();
        metainfo.put("ami_name", "CD-jenkins-machine");
        violationEntity = new ViolationEntity(null, null, null, null, metainfo, null, null, null, null, null);
        ruleEntity.setImageName(".+jenk.+");
        Boolean apply = evaluator.apply(ruleEntity, violationEntity);

        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testRegexEnd() throws Exception{
        Map<String, String> metainfo = newHashMap();
        metainfo.put("ami_name", "jenkins-machine");
        violationEntity = new ViolationEntity(null, null, null, null, metainfo, null, null, null, null, null);
        ruleEntity.setImageName("jenkins.+");
        Boolean apply = evaluator.apply(ruleEntity, violationEntity);

        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testNullRegex() throws Exception{
        Map<String, String> metainfo = newHashMap();
        violationEntity = new ViolationEntity(null, null, null, null, metainfo, null, null, null, null, null);
        ruleEntity.setImageName("jenkins.+");
        Boolean apply = evaluator.apply(ruleEntity, violationEntity);

        assertThat(apply).isEqualTo(false);
    }

    @Test
    public void testComplexRegex() throws Exception{
        Map<String, String> metainfo = newHashMap();
        metainfo.put("ami_name", "ELB-1234-ami-taupage");
        violationEntity = new ViolationEntity(null, null, null, null, metainfo, null, null, null, null, null);
        ruleEntity.setImageName("^.+-(\\d+)*taupage");
        Boolean apply = evaluator.apply(ruleEntity, violationEntity);

        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testComplexMatch() throws Exception{
        Map<String, String> metainfo = newHashMap();
        metainfo.put("application_version", "1.0-Snapshot");
        violationEntity = new ViolationEntity(null, "700123", "eu-central-6", null, metainfo, null, null, null, null, null);
        ruleEntity.setApplicationVersion("1.0-Snapshot");
        ruleEntity.setRegion("eu-central-6");
        ruleEntity.setAccountId("700123");
        Boolean apply = evaluator.apply(ruleEntity, violationEntity);

        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testComplexMatch2() throws Exception{
        Map<String, String> metainfo = newHashMap();
        metainfo.put("application_id", "myApp");
        violationEntity = new ViolationEntity(null, "700123", "eu-central-6", null, metainfo, null, null, null, null, null);
        violationEntity.setViolationTypeEntity(new ViolationTypeEntity("SOME_ID"));
        ruleEntity.setApplicationId("myApp");
        ruleEntity.setRegion("eu-central-6");
        ruleEntity.setAccountId("700123");
        ruleEntity.setViolationTypeEntityId("SOME_ID");
        Boolean apply = evaluator.apply(ruleEntity, violationEntity);

        assertThat(apply).isEqualTo(true);
    }
}