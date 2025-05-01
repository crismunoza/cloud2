package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Optional;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Function {

    // Endpoint POST para manejar los eventos de usuario (crear, actualizar, eliminar)
    @FunctionName("UserEventPublisher")
    public HttpResponseMessage userEventPublisher(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        context.getLogger().info("Procesando solicitud de evento de usuario.");

        String requestBody = request.getBody().orElse("");
        JSONObject json = new JSONObject(requestBody);

        String eventType = json.optString("eventType");

        if (eventType.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("El campo 'eventType' es requerido.")
                    .build();
        }

        try {
            JSONObject event = new JSONObject();
            event.put("id", UUID.randomUUID().toString());
            event.put("eventType", eventType); 
            event.put("subject", "users");
            event.put("data", json.getJSONObject("data"));  
            event.put("eventTime", java.time.Instant.now().toString());

            // Enviar el evento a Event Grid
            sendEventToEventGrid(event);
            context.getLogger().info("Evento enviado a Event Grid: " + event.toString());
            return request.createResponseBuilder(HttpStatus.ACCEPTED)
                    .body("Evento de usuario enviado exitosamente.")
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error al enviar el evento: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar el evento de usuario.")
                    .build();
        }
    }

    // Método para enviar el evento a EventGrid
    private void sendEventToEventGrid(JSONObject event) throws Exception {
        URL url = new URL("Link");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("aeg-sas-key", "<your_event_grid_key>"); 
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(("[" + event.toString() + "]").getBytes());
            os.flush();
        }

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK && conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
            throw new RuntimeException("Failed to send event: HTTP error code " + conn.getResponseCode());
        }
    }

    // Endpoint GET para obtener un usuario por ID
    @FunctionName("GetUser")
    public HttpResponseMessage getUser(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        context.getLogger().info("Procesando solicitud GetUser.");

        String idUsuario = request.getQueryParameters().get("id");
        System.out.println("ID Usuario: " + idUsuario);

        if (idUsuario == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Falta el parámetro requerido en la consulta: id.")
                    .build();
        }

        try (Connection conn = OracleDBConnection.getConnection()) {
            String sql = "SELECT * FROM USUARIO WHERE ID_USUARIO = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, Integer.parseInt(idUsuario));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        JSONObject user = new JSONObject();
                        user.put("id", rs.getInt("ID_USUARIO"));
                        user.put("nombre", rs.getString("NOMBRE"));
                        user.put("email", rs.getString("EMAIL"));
                        user.put("estado", rs.getInt("ESTADO"));
                        return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(user.toString()) 
                        .build();
                    } else {
                        return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                                .body("Usuario no encontrado.")
                                .build();
                                
                    }
                }
            }
        } catch (SQLException e) {
            context.getLogger().severe("Error al recuperar el usuario: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al recuperar el usuario.")
                    .build();
        }
    }

   @FunctionName("Get")
    public HttpResponseMessage getUsers(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        context.getLogger().info("Procesando solicitud GetUser.");

        try (Connection conn = OracleDBConnection.getConnection()) {
            String sql = "SELECT * FROM USUARIO";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    JSONArray usersArray = new JSONArray();
                    
                    while (rs.next()) {
                        JSONObject user = new JSONObject();
                        user.put("id", rs.getInt("ID_USUARIO"));
                        user.put("nombre", rs.getString("NOMBRE"));
                        user.put("email", rs.getString("EMAIL"));
                        user.put("estado", rs.getInt("ESTADO"));
                        usersArray.put(user);
                    }
                    
                    if (usersArray.length() > 0) {
                        return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(usersArray.toString()) 
                        .build();
                    } else {
                        return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                                .body("No se encontraron usuarios.")
                                .build();
                    }
                }
            }
        } catch (SQLException e) {
            context.getLogger().severe("Error al recuperar usuarios: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al recuperar usuarios.")
                    .build();
        }
    }
}
