package com.example.bff.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl("Link").build();
    }

    @Bean
    public WebClient roleWebClient(WebClient.Builder builder) {
        return builder.baseUrl("Link").build();
    }

    @Bean
    public WebClient graphQLUserWebClient(WebClient.Builder builder) {
        return builder.baseUrl("Link").build();
    }

    @Bean
    public WebClient graphQLRoleWebClient(WebClient.Builder builder) {
        return builder.baseUrl("Link").build();
    }
}

