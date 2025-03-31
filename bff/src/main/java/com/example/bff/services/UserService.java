package com.example.bff.services;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    private final WebClient webClient;

    @Value("${azure.functions.base-url}")
    private String baseUrl;

    public UserService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> getUserById(String id) {
        return webClient.get()
                .uri(baseUrl + "/GetUser?id=" + id) 
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> createUser(String userJson) {
        return webClient.post()
                .uri(baseUrl + "/CreateUser") 
                .bodyValue(userJson)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> updateUser(String id, String userJson) {
        return webClient.put()
                .uri(baseUrl + "/UpdateUser") 
                .bodyValue(userJson) 
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> deleteUser(String id) {
        return webClient.delete()
                .uri(baseUrl + "/DeleteUser?id=" + id) 
                .retrieve()
                .bodyToMono(String.class);
    }
}
