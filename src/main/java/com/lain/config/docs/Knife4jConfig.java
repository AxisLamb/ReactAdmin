package com.lain.config.docs;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;

@Configuration
public class Knife4jConfig {

    @Bean
    public UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder()
                .deepLinking(true)
                .displayOperationId(false)
                .defaultModelsExpandDepth(1)
                .defaultModelExpandDepth(1)
                .defaultModelRendering(springfox.documentation.swagger.web.ModelRendering.EXAMPLE)
                .displayRequestDuration(false)
                .docExpansion(springfox.documentation.swagger.web.DocExpansion.NONE)
                .filter(false)
                .maxDisplayedTags(null)
                .operationsSorter(springfox.documentation.swagger.web.OperationsSorter.ALPHA)
                .showExtensions(false)
                .showCommonExtensions(false)
                .tagsSorter(springfox.documentation.swagger.web.TagsSorter.ALPHA)
                .supportedSubmitMethods(UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS)
                .validatorUrl(null)
                .build();
    }
}