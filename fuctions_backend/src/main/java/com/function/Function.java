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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONObject;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    
    @FunctionName("HttpExample")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        final String query = request.getQueryParameters().get("name");
        final String name = request.getBody().orElse(query);

        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name).build();
        }
    }

    @FunctionName("CreateUser")
    public HttpResponseMessage createUser(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Processing CreateUser request.");

        String requestBody = request.getBody().orElse("");
        JSONObject json = new JSONObject(requestBody);

        String nombre = json.optString("nombre");
        String email = json.optString("email");
        String password = json.optString("password");

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Faltan campos requeridos: nombre, email o contraseña.")
                    .build();
        }

        try (Connection conn = OracleDBConnection.getConnection()) {
            String sql = "INSERT INTO USUARIO (NOMBRE, EMAIL, PASSWORD) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nombre);
                stmt.setString(2, email);
                stmt.setString(3, password);
                stmt.executeUpdate();
            }
            return request.createResponseBuilder(HttpStatus.CREATED)
                    .body("Usuario creado exitosamente.")
                    .build();
        } catch (SQLException e) {
            context.getLogger().severe("Error al crear el usuario: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el usuario.")
                    .build();
        }
    }

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

    @FunctionName("UpdateUser")
    public HttpResponseMessage updateUser(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Processing UpdateUser request.");

        String requestBody = request.getBody().orElse("");
        JSONObject json = new JSONObject(requestBody);

        int idUsuario = json.optInt("id", -1);
        String nombre = json.optString("nombre");
        String email = json.optString("email");
        String password = json.optString("password");

        if (idUsuario == -1 || nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Faltan campos requeridos: id, nombre, email o contraseña.")
                    .build();
        }

        try (Connection conn = OracleDBConnection.getConnection()) {
            String sql = "UPDATE USUARIO SET NOMBRE = ?, EMAIL = ?, PASSWORD = ? WHERE ID_USUARIO = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nombre);
                stmt.setString(2, email);
                stmt.setString(3, password);
                stmt.setInt(4, idUsuario);
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    return request.createResponseBuilder(HttpStatus.OK)
                            .body("Usuario actualizado exitosamente.")
                            .build();
                } else {
                    return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                            .body("Usuario no encontrado.")
                            .build();
                }
            }
        } catch (SQLException e) {
            context.getLogger().severe("Error al actualizar el usuario: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el usuario.")
                    .build();
        }
    }

    @FunctionName("DeleteUser")
    public HttpResponseMessage deleteUser(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Processing DeleteUser request.");

        String idUsuario = request.getQueryParameters().get("id");

        if (idUsuario == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Falta el parámetro requerido en la consulta: id.")
                    .build();
        }

        try (Connection conn = OracleDBConnection.getConnection()) {
            String sql = "DELETE FROM USUARIO WHERE ID_USUARIO = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, Integer.parseInt(idUsuario));
                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) {
                    return request.createResponseBuilder(HttpStatus.OK)
                            .body("Usuario eliminado exitosamente.")
                            .build();
                } else {
                    return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                            .body("Usuario no encontrado.")
                            .build();
                }
            }
        } catch (SQLException e) {
            context.getLogger().severe("Error al eliminar el usuario: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el usuario.")
                    .build();
        }
    }
}
