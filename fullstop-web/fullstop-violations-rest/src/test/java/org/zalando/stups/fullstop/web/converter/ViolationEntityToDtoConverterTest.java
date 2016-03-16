package org.zalando.stups.fullstop.web.converter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.convert.converter.Converter;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.violation.entity.ViolationEntity;
import org.zalando.stups.fullstop.web.model.Violation;

import static org.assertj.core.api.Assertions.assertThat;

public class ViolationEntityToDtoConverterTest {

    public static final String ACCOUNT_ID = "123";
    public static final String COMMENT = "cc";
    public static final RuleEntity RULE_ENTITY = null;

    private ViolationEntity violationEntity = new ViolationEntity();


    private Converter<ViolationEntity, Violation> entityToDto = new ViolationEntityToDtoConverter(new ViolationTypeEntityToDtoConverter());

    @Before
    public void setUp() throws Exception {
        violationEntity.setAccountId(ACCOUNT_ID);
        violationEntity.setComment(COMMENT);
        violationEntity.setRuleEntity(RULE_ENTITY);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testConvertNullPointer() throws Exception {
        Violation dto = entityToDto.convert(violationEntity);
    }

    @Test
    public void testConvert() throws Exception {
        RuleEntity ruleEntity = new RuleEntity();
        ruleEntity.setId(1L);
        violationEntity.setRuleEntity(ruleEntity);
        Violation dto = entityToDto.convert(violationEntity);

        assertThat(dto.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(dto.getComment()).isEqualTo(COMMENT);
        assertThat(dto.getRuleID()).isEqualTo(1L);
    }
}