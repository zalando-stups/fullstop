package org.zalando.stups.fullstop.web.controller;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.zalando.stups.fullstop.violation.entity.ViolationTypeEntity;
import org.zalando.stups.fullstop.violation.repository.ViolationTypeRepository;
import org.zalando.stups.fullstop.web.model.ViolationType;
import org.zalando.stups.fullstop.web.test.ControllerTestConfig;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.zalando.stups.fullstop.web.test.MatcherHelper.hasSize;


@ContextConfiguration
@RunWith(SpringRunner.class)
@WebAppConfiguration
public class ViolationTypesControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ViolationTypeRepository mockViolationTypeRepository;

    @Autowired
    private Converter<ViolationTypeEntity, ViolationType> mockConverter;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        reset(mockViolationTypeRepository, mockConverter);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).alwaysDo(print()).build();

        when(mockViolationTypeRepository.findAll())
                .thenReturn(asList(new ViolationTypeEntity("OOPS"), new ViolationTypeEntity("SHIT_HAPPENS")));

        when(mockConverter.convert(any())).then(invocationOnMock -> {
            final ViolationTypeEntity input = (ViolationTypeEntity) invocationOnMock.getArguments()[0];
            final ViolationType result = new ViolationType();
            result.setId(input.getId());
            result.setPriority(3);
            return result;
        });
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockViolationTypeRepository, mockConverter);
    }

    @Test
    public void testGetViolationTypes() throws Exception {
        mockMvc.perform(get("/api/violation-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("OOPS"))
                .andExpect(jsonPath("$[1].id").value("SHIT_HAPPENS"))
                .andExpect(jsonPath("$[1].priority").value(3));

        verify(mockViolationTypeRepository).findAll();
        verify(mockConverter, times(2)).convert(any());
    }

    @Configuration
    @Import(ControllerTestConfig.class)
    static class TestConfig {

        @Bean
        ViolationTypesController violationTypesController(final ViolationTypeRepository violationTypeRepository, final Converter<ViolationTypeEntity, ViolationType> converter) {
            return new ViolationTypesController(violationTypeRepository, converter);
        }

        @Bean
        ViolationTypeRepository violationTypeRepository() {
            return mock(ViolationTypeRepository.class);
        }

        @Bean
        @SuppressWarnings("unchecked")
        Converter<ViolationTypeEntity, ViolationType> converter() {
            return mock(Converter.class);
        }
    }
}
