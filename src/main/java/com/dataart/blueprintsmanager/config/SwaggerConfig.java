package com.dataart.blueprintsmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
public class SwaggerConfig {
    public static final String DEFAULT_INCLUDE_PATTERN = "/api/.*";

    @Bean
    public Docket swaggerSpringfoxDocket() {

        return new Docket(DocumentationType.SWAGGER_2)
                .forCodeGeneration(true)
                .genericModelSubstitutes(ResponseEntity.class)
                .select()
                .paths(regex(DEFAULT_INCLUDE_PATTERN))
                .build();
    }
}
