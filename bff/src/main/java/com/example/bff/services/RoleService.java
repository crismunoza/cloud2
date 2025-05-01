package com.example.bff.services;

import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
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
    
    public Mono<String> getAllRol() {
        return roleWebClient.get()
                .uri(baseUrl + "/Get") 
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getRolesByUserId(String idUser) {
        return roleWebClient.get()
                .uri(baseUrl + "/GetRelaRole?idUser=" + idUser)
                .retrieve()
                .bodyToMono(String.class);
    }

    //aca empezamos a crear el evento
    public Mono<String> sendUserEvent(String userJson) {
        JSONObject json = new JSONObject(userJson);
        String eventType = json.optString("eventType");
        
        String path = "/RoleEventPublisher";  
        try {
            String eventJson = createEventJson(eventType, userJson);
            return sendEvent(eventJson, path);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error creando el payload del evento", e));
        }
    }

    private String createEventJson(String eventType, String userJson) throws JSONException {
        JSONObject json = new JSONObject(userJson);
        JSONObject event = new JSONObject();
        event.put("id", UUID.randomUUID().toString());  
        event.put("eventType", eventType);  
        event.put("subject", "users");  
        event.put("data", json.getJSONObject("data"));  
        event.put("eventTime", java.time.Instant.now().toString());  
        
        return event.toString();
    }

    private Mono<String> sendEvent(String eventJson, String path) {
        System.out.println("Event sent: " + eventJson); 
        return roleWebClient.post()
                .uri(baseUrl + path)
                .bodyValue(eventJson)
                .retrieve()
                .bodyToMono(String.class); 
    }
}
