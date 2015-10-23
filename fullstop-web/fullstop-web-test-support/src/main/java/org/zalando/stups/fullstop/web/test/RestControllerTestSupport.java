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
package org.zalando.stups.fullstop.web.test;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.web.bind.support.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

public abstract class RestControllerTestSupport {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected MockMvc mockMvc;

    public RestControllerTestSupport() {
        objectMapper.registerModules();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @Before
    public void setUpMockMvc() throws Exception {
        final StandaloneMockMvcBuilder mockMvcBuilder = MockMvcBuilders.standaloneSetup(mockMvcControllers());
        configure(mockMvcBuilder);
        mockMvc = mockMvcBuilder.build();
    }

    protected abstract Object[] mockMvcControllers();

    protected void configure(final StandaloneMockMvcBuilder mockMvcBuilder) {
        mockMvcBuilder.setCustomArgumentResolvers(mockMvcCustomArgumentResolvers());
        mockMvcBuilder.setMessageConverters(mockMvcMessageConverters());

        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        mockMvcBuilder.setConversionService(conversionService);
    }

    protected HttpMessageConverter<?>[] mockMvcMessageConverters() {
        final MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setObjectMapper(objectMapper);

        return new HttpMessageConverter[] { jsonConverter, new ResourceHttpMessageConverter() };
    }

    protected HandlerMethodArgumentResolver[] mockMvcCustomArgumentResolvers() {
        final PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        pageableResolver.setFallbackPageable(new PageRequest(0, 10));

        final AuthenticationPrincipalArgumentResolver authenticationPrincipalResolver = new AuthenticationPrincipalArgumentResolver();

        return new HandlerMethodArgumentResolver[] { pageableResolver, authenticationPrincipalResolver };
    }
}
