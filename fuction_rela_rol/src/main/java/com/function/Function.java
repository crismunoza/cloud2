package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URL;

public class Function {

    @FunctionName("RoleEventPublisher")
    public HttpResponseMessage roleEventPublisher(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        
        context.getLogger().info("Procesando solicitud de evento de Rol.");

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
                    .body("Evento de Rol enviado exitosamente.")
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Error al enviar el evento: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar el evento de Rol.")
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

    @FunctionName("GetRelaRole")
    public HttpResponseMessage getUserRoles(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Procesando solicitud para obtener relaciones usuario-rol.");

        String idUser = request.getQueryParameters().get("idUser");

        if (idUser == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Falta el parámetro requerido: idUser.")
                    .build();
        }

        try (Connection conn = OracleDBConnection.getConnection()) {
            String sql = "SELECT C.ID_ROL AS ID_ROL, C.NOMBRE AS NOMBRE_ROL FROM USUARIO A, USUARIO_ROL B, ROL C WHERE A.ID_USUARIO = B.ID_USUARIO AND B.ID_ROL = C.ID_ROL AND B.ID_USUARIO = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, Integer.parseInt(idUser));
                try (ResultSet rs = stmt.executeQuery()) {
                    JSONArray roles = new JSONArray();
                    while (rs.next()) {
                        JSONObject role = new JSONObject();
                        role.put("nombreRol", rs.getString("NOMBRE_ROL"));
                        role.put("idRol", rs.getInt("ID_ROL"));
                        roles.put(role);
                    }
                    return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(roles.toString()) 
                    .build();
                            
                }
            }
        } catch (SQLException e) {
            context.getLogger().severe("Error al obtener las relaciones usuario-rol: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener las relaciones usuario-rol.")
                    .build();
        }
    }

    @FunctionName("Get")
    public HttpResponseMessage get(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Procesando solicitud para obtener relaciones usuario-rol.");

        try (Connection conn = OracleDBConnection.getConnection()) {
            String sql = "SELECT * FROM USUARIO_ROL";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    JSONArray roles = new JSONArray();
                    while (rs.next()) {
                        JSONObject role = new JSONObject();
                        role.put("idUser", rs.getString("ID_USUARIO"));
                        role.put("idRol", rs.getInt("ID_ROL"));
                        roles.put(role);
                    }
                    return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(roles.toString()) 
                    .build();
                }
            }
        } catch (SQLException e) {
            context.getLogger().severe("Error al obtener las relaciones usuario-rol: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener las relaciones usuario-rol.")
                    .build();
        }
    }

}
