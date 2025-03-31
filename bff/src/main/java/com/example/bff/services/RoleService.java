package com.example.bff.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class RoleService {

    private final WebClient roleWebClient;

    @Value("${azure.functions.base-url-2}")
    private String baseUrl;

    public RoleService(WebClient roleWebClient) {
        this.roleWebClient = roleWebClient;
    }

    public Mono<String> createRole(String roleJson) {
        return roleWebClient.post()
                .uri(baseUrl + "/CreateRelaRole")
                .bodyValue(roleJson)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getRolesByUserId(String idUser) {
        return roleWebClient.get()
                .uri(baseUrl + "/GetRelaRole?idUser=" + idUser)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> updateRole(String roleJson) {
        return roleWebClient.put()
                .uri(baseUrl + "/UpdateRelaRole")
                .bodyValue(roleJson)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> deleteRole(String idUser, String idRol) {
        return roleWebClient.delete()
                .uri(baseUrl + "/DeleteRelaRole?idUser=" + idUser + "&idRol=" + idRol)
                .retrieve()
                .bodyToMono(String.class);
    }
}
