package org.zalando.stups.fullstop.swagger.configuration;

import com.google.common.base.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

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
                "",
                "Apache 2.0",
                "http://www.apache.org/licenses/LICENSE-2.0.html");
    }

    @Bean
    public Docket customImplementation() {
        return new Docket(DocumentationType.SWAGGER_2) //
                .apiInfo(apiInfo())                    //
                .select()                              //
                .paths(fullstopOnlyEndpoints())        //
                .build();
    }

    private Predicate<String> fullstopOnlyEndpoints() {
        return input -> input.contains("/api/");
    }

}
