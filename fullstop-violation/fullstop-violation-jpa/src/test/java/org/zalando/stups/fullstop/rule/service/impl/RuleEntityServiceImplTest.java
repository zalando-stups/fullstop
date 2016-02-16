package org.zalando.stups.fullstop.rule.service.impl;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.zalando.fullstop.web.api.NotFoundException;
import org.zalando.stups.fullstop.rule.entity.RuleDTO;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.rule.repository.RuleEntityRepository;
import org.zalando.stups.fullstop.rule.service.RuleEntityService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class RuleEntityServiceImplTest {

    @Autowired
    private RuleEntityRepository ruleEntityRepository;

    @Autowired
    private RuleEntityService ruleEntityServiceImpl;

    private RuleEntity ruleEntity;

    @Before
    public void setUp() throws Exception {
        reset(ruleEntityRepository);

        ruleEntity = new RuleEntity();
        ruleEntity.setId(1L);
        ruleEntity.setAccountId("1234");
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(ruleEntityRepository);
    }

    @Test
    public void testSave() throws Exception {
        RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setAccountId("1234");
        when(ruleEntityRepository.save(any(RuleEntity.class))).then(AdditionalAnswers.returnsFirstArg());
        RuleEntity savedRuleEntity = ruleEntityServiceImpl.save(ruleDTO);

        assertThat(savedRuleEntity.getAccountId()).isEqualTo("1234");
        assertThat(savedRuleEntity.getExpiryDate()).isEqualByComparingTo(new DateTime(Long.MAX_VALUE));

        verify(ruleEntityRepository).save(any(RuleEntity.class));

    }

    @Test
    public void testUpdate() throws Exception {
        RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setAccountId("5678");
        when(ruleEntityRepository.findOne(anyLong())).thenReturn(ruleEntity);
        when(ruleEntityRepository.save(any(RuleEntity.class))).then(AdditionalAnswers.returnsFirstArg());

        RuleEntity updatedRule = ruleEntityServiceImpl.update(ruleDTO, 1L);
        assertThat(updatedRule.getAccountId()).isEqualToIgnoringCase("5678");
        assertThat(updatedRule.getExpiryDate()).isEqualByComparingTo(new DateTime(Long.MAX_VALUE));

        verify(ruleEntityRepository).findOne(anyLong());
        verify(ruleEntityRepository, times(2)).save(any(RuleEntity.class));
    }

    @Test(expected = NotFoundException.class)
    public void testUpdateFails() throws Exception {
        RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setAccountId("5678");
        when(ruleEntityRepository.findOne(anyLong())).thenReturn(null);
        try {

            RuleEntity updatedRule = ruleEntityServiceImpl.update(ruleDTO, 1L);
        } finally {

            verify(ruleEntityRepository).findOne(anyLong());
        }

    }

    @Test
    public void testFindByIdFails() throws Exception {
        when(ruleEntityRepository.findOne(anyLong())).thenReturn(null);

        RuleEntity updatedRule = ruleEntityServiceImpl.findById(1L);
        assertThat(updatedRule).isEqualTo(null);

        verify(ruleEntityRepository).findOne(anyLong());
    }

    @Test
    public void testFindByNotExpired() throws Exception {

    }

    @Test
    public void testFindAll() throws Exception {

    }

    @Configuration
    static class TestConfig {

        @Bean
        public RuleEntityService ruleEntityServiceImpl() {
            return new RuleEntityServiceImpl();
        }

        @Bean
        public RuleEntityRepository ruleEntityRepository() {
            return mock(RuleEntityRepository.class);
        }
    }
}