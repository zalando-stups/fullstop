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
import org.zalando.stups.fullstop.rule.entity.RuleDTO;
import org.zalando.stups.fullstop.rule.entity.RuleEntity;
import org.zalando.stups.fullstop.rule.repository.RuleEntityRepository;
import org.zalando.stups.fullstop.rule.service.RuleEntityService;

import java.util.List;
import java.util.NoSuchElementException;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.joda.time.DateTimeZone.UTC;
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
        assertThat(savedRuleEntity.getExpiryDate()).isEqualTo(new DateTime(9999, 1, 1, 0, 0, 0, UTC));

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
        assertThat(updatedRule.getExpiryDate()).isEqualByComparingTo(new DateTime(9999, 1, 1, 0, 0, 0, UTC));

        verify(ruleEntityRepository).findOne(anyLong());
        verify(ruleEntityRepository, times(2)).save(any(RuleEntity.class));
    }

    @Test(expected = NoSuchElementException.class)
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
        when(ruleEntityRepository.findByExpiryDateAfter(any(DateTime.class))).thenReturn(newArrayList(ruleEntity));

        List<RuleEntity> byNotExpired = ruleEntityServiceImpl.findByNotExpired();
        assertThat(byNotExpired).hasSize(1);

        verify(ruleEntityRepository).findByExpiryDateAfter(any(DateTime.class));
    }

    @Test
    public void testFindAll() throws Exception {
        when(ruleEntityRepository.findAll()).thenReturn(newArrayList(ruleEntity));
        List<RuleEntity> ruleEntities = ruleEntityServiceImpl.findAll();
        assertThat(ruleEntities).hasSize(1);

        verify(ruleEntityRepository).findAll();

    }

    @Test
    public void testExpireSuccessfully() throws Exception {
        final RuleEntity re = mock(RuleEntity.class);
        when(re.getExpiryDate()).thenReturn(DateTime.now().plusDays(2));
        when(ruleEntityRepository.findOne(anyLong())).thenReturn(re);

        ruleEntityServiceImpl.expire(1L, DateTime.now().plusDays(1));

        verify(ruleEntityRepository).findOne(anyLong());
        verify(ruleEntityRepository).save(re);
    }

    @Test
    public void testExpireWithNull() throws Exception {
        ruleEntityServiceImpl.expire(1L, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExpireExpiryDateInPast() throws Exception {
        final DateTime now = DateTime.now().plusDays(1);
        ruleEntity.setExpiryDate(now);
        when(ruleEntityRepository.findOne(anyLong())).thenReturn(ruleEntity);

        try {
            final DateTime expiryDate = DateTime.now().minusDays(1);
            ruleEntityServiceImpl.expire(1L, expiryDate);
        } finally {
            verify(ruleEntityRepository).findOne(anyLong());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExpireOldExpiryDateInPast() throws Exception {
        final DateTime now = DateTime.now().minusDays(1);
        ruleEntity.setExpiryDate(now);
        when(ruleEntityRepository.findOne(anyLong())).thenReturn(ruleEntity);

        try {
            final DateTime expiryDate = DateTime.now().plusDays(1);
            ruleEntityServiceImpl.expire(1L, expiryDate);
        } finally {
            verify(ruleEntityRepository).findOne(anyLong());
        }
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