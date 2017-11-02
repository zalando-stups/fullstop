package org.zalando.stups.fullstop.whitelist;

import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.violation.entity.ApplicationEntity;
import org.zalando.stups.fullstop.violation.entity.VersionEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static org.assertj.core.api.Assertions.assertThat;

public class WhitelistRulesEvaluatorTest {

    private ViolationEntity violationEntity;
    private RuleEntity ruleEntity;
    private WhitelistRulesEvaluator evaluator;

    @Before
    public void setUp() throws Exception {
        ruleEntity = new RuleEntity();
        evaluator = new WhitelistRulesEvaluator();

    }


    @Test
    public void testAccountID() throws Exception {
        violationEntity = new ViolationEntity(null, "1234", null, null, null, null, null, null, null, null);
        ruleEntity.setAccountId("1234");
        final Boolean apply = evaluator.apply(ruleEntity, violationEntity);
        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testRegion() throws Exception {
        violationEntity = new ViolationEntity(null, null, "eu-west-1", null, null, null, null, null, null, null);
        ruleEntity.setRegion("eu-west-1");

        final Boolean apply = evaluator.apply(ruleEntity, violationEntity);
        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testViolationType() throws Exception {
        violationEntity = new ViolationEntity(null, null, null, null, null, null, null, null, null, null);
        violationEntity.setViolationTypeEntity(new ViolationTypeEntity("NOT_FOUND_IN_KIO"));
        ruleEntity.setViolationTypeEntityId("NOT_FOUND_IN_KIO");

        final Boolean apply = evaluator.apply(ruleEntity, violationEntity);
        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testImageOwner() throws Exception {
        final Map<String, String> metainfo = newHashMap();
        metainfo.put("ami_owner_id", "Team");
        violationEntity = new ViolationEntity(null, null, null, null, metainfo, null, null, null, null, null);
        ruleEntity.setImageOwner("Team");

        final Boolean apply = evaluator.apply(ruleEntity, violationEntity);
        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testApplicationID() throws Exception {
        violationEntity = new ViolationEntity(null, null, null, null, null, null, null, null, null, null);
        violationEntity.setApplication(new ApplicationEntity("myApp"));
        ruleEntity.setApplicationId("myApp");

        final Boolean apply = evaluator.apply(ruleEntity, violationEntity);
        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testApplicationVersion() throws Exception {
        violationEntity = new ViolationEntity(null, null, null, null, null, null, null, null, null, null);
        violationEntity.setApplicationVersion(new VersionEntity("1.0-Snapshot"));
        ruleEntity.setApplicationVersion("1.0-Snapshot");

        final Boolean apply = evaluator.apply(ruleEntity, violationEntity);
        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testNullMetaInfo() throws Exception {
        final Map<String, String> metainfo = newHashMap();
        violationEntity = new ViolationEntity(null, null, null, null, metainfo, null, null, null, null, null);
        ruleEntity.setImageOwner("Peter");

        final Boolean apply = evaluator.apply(ruleEntity, violationEntity);
        assertThat(apply).isEqualTo(false);
    }

    @Test
    public void testRegexStart() throws Exception{
        final Map<String, String> metainfo = newHashMap();
        metainfo.put("ami_name", "CD-jenkins");
        violationEntity = new ViolationEntity(null, null, null, null, metainfo, null, null, null, null, null);
        ruleEntity.setImageName(".+jenkins");
        final Boolean apply = evaluator.apply(ruleEntity, violationEntity);

        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testRegexMiddle() throws Exception{
        final Map<String, String> metainfo = newHashMap();
        metainfo.put("ami_name", "CD-jenkins-machine");
        violationEntity = new ViolationEntity(null, null, null, null, metainfo, null, null, null, null, null);
        ruleEntity.setImageName(".+jenk.+");
        final Boolean apply = evaluator.apply(ruleEntity, violationEntity);

        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testRegexEnd() throws Exception{
        final Map<String, String> metainfo = newHashMap();
        metainfo.put("ami_name", "jenkins-machine");
        violationEntity = new ViolationEntity(null, null, null, null, metainfo, null, null, null, null, null);
        ruleEntity.setImageName("jenkins.+");
        final Boolean apply = evaluator.apply(ruleEntity, violationEntity);

        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testNullRegex() throws Exception{
        final Map<String, String> metainfo = newHashMap();
        violationEntity = new ViolationEntity(null, null, null, null, metainfo, null, null, null, null, null);
        ruleEntity.setImageName("jenkins.+");
        final Boolean apply = evaluator.apply(ruleEntity, violationEntity);

        assertThat(apply).isEqualTo(false);
    }

    @Test
    public void testComplexRegex() throws Exception{
        final Map<String, String> metainfo = newHashMap();
        metainfo.put("ami_name", "ELB-1234-ami-taupage");
        violationEntity = new ViolationEntity(null, null, null, null, metainfo, null, null, null, null, null);
        ruleEntity.setImageName("^.+-(\\d+)*taupage");
        final Boolean apply = evaluator.apply(ruleEntity, violationEntity);

        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testComplexMatch() throws Exception{
        final Map<String, String> metainfo = newHashMap();
        metainfo.put("ami_owner_id", "id-1234");
        violationEntity = new ViolationEntity(null, "700123", "eu-central-6", null, metainfo, null, null, null, null, null);
        ruleEntity.setImageOwner("id-1234");
        ruleEntity.setRegion("eu-central-6");
        ruleEntity.setAccountId("700123");
        final Boolean apply = evaluator.apply(ruleEntity, violationEntity);

        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testComplexMatch2() throws Exception{
        final Map<String, String> metainfo = newHashMap();
        metainfo.put("ami_owner_id", "id-1234");
        violationEntity = new ViolationEntity(null, "700123", "eu-central-6", null, metainfo, null, null, null, null, null);
        violationEntity.setViolationTypeEntity(new ViolationTypeEntity("SOME_ID"));
        ruleEntity.setImageOwner("id-1234");
        ruleEntity.setRegion("eu-central-6");
        ruleEntity.setAccountId("700123");
        ruleEntity.setViolationTypeEntityId("SOME_ID");
        final Boolean apply = evaluator.apply(ruleEntity, violationEntity);

        assertThat(apply).isEqualTo(true);
    }

    @Test
    public void testJsonPath() throws Exception {
        final HashMap<String, String> metaInfo = newHashMap();
        metaInfo.put("user_name", "ses.ci-master-foobar-123abc");
        violationEntity = new ViolationEntity(null, "700123", "eu-central-6", null, metaInfo, null, null, null, null, null);
        ruleEntity.setMetaInfoJsonPath("$.[?(@.user_name =~ /ses\\.ci-master-.+/)]");

        assertThat(evaluator.apply(ruleEntity, violationEntity)).isTrue();
    }

    @Test
    public void testJsonPathMismatch() throws Exception {
        final HashMap<String, String> metaInfo = newHashMap();
        metaInfo.put("user_name", "ses.ci-slave");
        violationEntity = new ViolationEntity(null, "700123", "eu-central-6", null, metaInfo, null, null, null, null, null);
        ruleEntity.setMetaInfoJsonPath("$.[?(@.user_name =~ /ses\\.ci-master-.+/)]");

        assertThat(evaluator.apply(ruleEntity, violationEntity)).isFalse();
    }

    @Test
    public void testJsonPathMismatch2() throws Exception {
        final HashMap<String, Object> metaInfo = newHashMap();
        metaInfo.put("foo", 1);
        violationEntity = new ViolationEntity(null, "700123", "eu-central-6", null, metaInfo, null, null, null, null, null);
        ruleEntity.setMetaInfoJsonPath("$.[?(@.user_name =~ /ses\\.ci-master-.+/)]");

        assertThat(evaluator.apply(ruleEntity, violationEntity)).isFalse();
    }
}
