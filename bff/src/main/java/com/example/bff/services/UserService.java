package com.example.bff.services;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Service
public class UserService {

    private final WebClient webClient;


    @Value("${azure.functions.base-url}")
    private String baseUrl;

    public UserService(WebClient webClient) {
        this.webClient = webClient;

    }

    public Mono<String> getAllUsers() {
        return webClient.get()
                .uri(baseUrl + "/Get") 
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> getUserById(String id) {
        return webClient.get()
                .uri(baseUrl + "/GetUser?id=" + id) 
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> sendUserEvent(String userJson) {
        // Parseamos el JSON que se recibe para obtener el 'eventType'
        JSONObject json = new JSONObject(userJson);
        String eventType = json.optString("eventType");
        
        // Verificamos el tipo de evento y decidimos qué hacer
        String path = "/UserEventPublisher";  
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
        return webClient.post()
                .uri(baseUrl + path)
                .bodyValue(eventJson)
                .retrieve()
                .bodyToMono(String.class); 
    }

    
}
