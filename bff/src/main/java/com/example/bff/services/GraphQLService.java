package com.example.bff.services;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GraphQLService {

    private final WebClient graphQLUserWebClient;
    private final WebClient graphQLRoleWebClient;

    public GraphQLService(WebClient graphQLUserWebClient, WebClient graphQLRoleWebClient) {
        this.graphQLUserWebClient = graphQLUserWebClient;
        this.graphQLRoleWebClient = graphQLRoleWebClient;
    }

    public Mono<String> executeUserGraphQLQuery(String query, Object variables) {
        return graphQLUserWebClient.post()
                .bodyValue(createGraphQLRequest(query, variables))
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> executeRoleGraphQLQuery(String query, Object variables) {
        return graphQLRoleWebClient.post()
                .bodyValue(createGraphQLRequest(query, variables))
                .retrieve()
                .bodyToMono(String.class);
    }

    private Object createGraphQLRequest(String query, Object variables) {
        return new GraphQLRequest(query, variables);
    }

    public static class GraphQLRequest {
        private String query;
        private Object variables;

        public GraphQLRequest(String query, Object variables) {
            this.query = query;
            this.variables = variables;
        }

        public String getQuery() {
            return query;
        }

        public Object getVariables() {
            return variables;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public void setVariables(Object variables) {
            this.variables = variables;
        }
    }
}
