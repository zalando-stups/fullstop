/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.web.controller;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.stups.fullstop.violation.entity.CountByAccountAndType;
import org.zalando.stups.fullstop.violation.repository.ViolationRepository;

import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Boolean.FALSE;
import static java.util.Collections.emptySet;
import static java.util.Optional.empty;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.zalando.stups.fullstop.web.test.MatcherHelper.hasSize;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public class ViolationsCountControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ViolationRepository mockViolationRepository;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        reset(mockViolationRepository);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).alwaysDo(print()).build();
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockViolationRepository);
    }

    @Test
    public void testCountAllByAccountAndType() throws Exception {
        when(mockViolationRepository.countByAccountAndType(any(), any(), any(), any()))
                .thenReturn(newArrayList(
                        new CountByAccountAndType("acc01", "oops", 40),
                        new CountByAccountAndType("acc01", "bla", 10),
                        new CountByAccountAndType("acc02", "oops", 15)));

        mockMvc.perform(get("/api/violation-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)));

        verify(mockViolationRepository).countByAccountAndType(eq(emptySet()), eq(empty()), eq(empty()), eq(empty()));
    }

    @Test
    public void testCountFilteredByAccountAndType() throws Exception {
        final DateTime from = DateTime.now();
        final DateTime to = DateTime.now();

        when(mockViolationRepository.countByAccountAndType(any(), any(), any(), any()))
                .thenReturn(newArrayList(
                        new CountByAccountAndType("acc01", "oops", 40),
                        new CountByAccountAndType("acc01", "bla", 10),
                        new CountByAccountAndType("acc02", "oops", 15)));

        mockMvc.perform(get("/api/violation-count")
                .param("accounts", "acc01,acc02").param("resolved", "false").param("from", from.toString()).param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)));

        verify(mockViolationRepository).countByAccountAndType(eq(newHashSet("acc01", "acc02")), eq(Optional.of(from)), eq(Optional.of(to)), eq(Optional.of(FALSE)));
    }

    @Configuration
    @Import(ControllerTestConfig.class)
    static class TestConfig {

        @Bean
        ViolationRepository violationRepository() {
            return mock(ViolationRepository.class);
        }

        @Bean
        ViolationsCountController violationsCountController(ViolationRepository violationRepository) {
            return new ViolationsCountController(violationRepository);
        }
    }
}
