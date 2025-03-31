package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Azure Functions con HTTP Trigger para gestionar la relación entre usuarios y roles.
 */
public class Function {
    /**
     * Esta función escucha en el endpoint "/api/HttpExample". Dos maneras de invocarla usando el comando "curl" en bash:
     * 1. curl -d "HTTP Body" {tu host}/api/HttpExample
     * 2. curl "{tu host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("HttpExample")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger procesó una solicitud.");

        // Parsear parámetro de consulta
        final String query = request.getQueryParameters().get("name");
        final String name = request.getBody().orElse(query);

        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Por favor, proporciona un nombre en la cadena de consulta o en el cuerpo de la solicitud").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Hola, " + name).build();
        }
    }

    @FunctionName("CreateRelaRole")
    public HttpResponseMessage createUserRole(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Procesando solicitud para crear relación usuario-rol.");

        String requestBody = request.getBody().orElse("");
        JSONObject json = new JSONObject(requestBody);

        int idUser = json.optInt("idUser", -1);
        int idRol = json.optInt("idRol", -1);

        if (idUser == -1 || idRol == -1) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Faltan campos requeridos: idUser o idRol.")
                    .build();
        }

        try (Connection conn = OracleDBConnection.getConnection()) {
            String sql = "INSERT INTO USUARIO_ROL (ID_USUARIO, ID_ROL) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, idUser);
                stmt.setInt(2, idRol);
                stmt.executeUpdate();
            }
            return request.createResponseBuilder(HttpStatus.CREATED)
                    .body("Relación usuario-rol creada exitosamente.")
                    .build();
        } catch (SQLException e) {
            context.getLogger().severe("Error al crear la relación usuario-rol: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear la relación usuario-rol.")
                    .build();
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
            String sql = "SELECT * FROM USUARIO_ROL WHERE ID_USUARIO = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, Integer.parseInt(idUser));
                try (ResultSet rs = stmt.executeQuery()) {
                    JSONArray roles = new JSONArray();
                    while (rs.next()) {
                        JSONObject role = new JSONObject();
                        role.put("idUser", rs.getInt("ID_USUARIO"));
                        role.put("idRol", rs.getInt("ID_ROL"));
                        roles.put(role);
                    }
                    return request.createResponseBuilder(HttpStatus.OK)
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

    @FunctionName("UpdateRelaRole")
    public HttpResponseMessage updateUserRole(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Procesando solicitud para actualizar relación usuario-rol.");

        String requestBody = request.getBody().orElse("");
        JSONObject json = new JSONObject(requestBody);

        int idUser = json.optInt("idUser", -1);
        int oldIdRol = json.optInt("oldIdRol", -1);
        int newIdRol = json.optInt("newIdRol", -1);

        if (idUser == -1 || oldIdRol == -1 || newIdRol == -1) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Faltan campos requeridos: idUser, oldIdRol o newIdRol.")
                    .build();
        }

        try (Connection conn = OracleDBConnection.getConnection()) {
            String sql = "UPDATE USUARIO_ROL SET ID_ROL = ? WHERE ID_USUARIO = ? AND ID_ROL = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, newIdRol);
                stmt.setInt(2, idUser);
                stmt.setInt(3, oldIdRol);
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated > 0) {
                    return request.createResponseBuilder(HttpStatus.OK)
                            .body("Relación usuario-rol actualizada exitosamente.")
                            .build();
                } else {
                    return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                            .body("Relación usuario-rol no encontrada.")
                            .build();
                }
            }
        } catch (SQLException e) {
            context.getLogger().severe("Error al actualizar la relación usuario-rol: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar la relación usuario-rol.")
                    .build();
        }
    }

    @FunctionName("DeleteRelaRole")
    public HttpResponseMessage deleteUserRole(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Procesando solicitud para eliminar relación usuario-rol.");

        String idUser = request.getQueryParameters().get("idUser");
        String idRol = request.getQueryParameters().get("idRol");

        if (idUser == null || idRol == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Faltan parámetros requeridos: idUser o idRol.")
                    .build();
        }

        try (Connection conn = OracleDBConnection.getConnection()) {
            String sql = "DELETE FROM USUARIO_ROL WHERE ID_USUARIO = ? AND ID_ROL = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, Integer.parseInt(idUser));
                stmt.setInt(2, Integer.parseInt(idRol));
                int rowsDeleted = stmt.executeUpdate();
                if (rowsDeleted > 0) {
                    return request.createResponseBuilder(HttpStatus.OK)
                            .body("Relación usuario-rol eliminada exitosamente.")
                            .build();
                } else {
                    return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                            .body("Relación usuario-rol no encontrada.")
                            .build();
                }
            }
        } catch (SQLException e) {
            context.getLogger().severe("Error al eliminar la relación usuario-rol: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la relación usuario-rol.")
                    .build();
        }
    }
}
