package org.zalando.stups.fullstop.swagger.configuration;

import org.joda.time.DateTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static java.util.Collections.emptyList;

@Configuration
@PropertySource("classpath:swagger.properties")
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    ApiInfo apiInfo() {
        return new ApiInfo(
                "Fullstop API",
                "Audit reporting",
                "",
                "",
                ApiInfo.DEFAULT_CONTACT,
                "Apache 2.0",
                "http://www.apache.org/licenses/LICENSE-2.0.html",
                emptyList());
    }

    @Bean
    public Docket customImplementation() {
        return new Docket(DocumentationType.SWAGGER_2)
                .directModelSubstitute(DateTime.class, String.class)
                .apiInfo(apiInfo())
                .select()
                .paths(input -> input != null && input.contains("/api/"))
                .build();
    }

}
