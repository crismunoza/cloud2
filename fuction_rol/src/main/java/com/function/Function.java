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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
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

    @FunctionName("CreateRol")
    public HttpResponseMessage createRol(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Creando un nuevo rol.");

        String rolName = request.getBody().orElse(null);
        if (rolName == null || rolName.isEmpty()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("El nombre del rol es obligatorio.").build();
        }

        try (Connection connection = DriverManager.getConnection("jdbc:your_database_url", "username", "password");
             PreparedStatement statement = connection.prepareStatement("INSERT INTO SPRING_BOOT_EXP1.ROL (NOMBRE) VALUES (?)")) {
            statement.setString(1, rolName);
            statement.executeUpdate();
            return request.createResponseBuilder(HttpStatus.CREATED).body("Rol creado exitosamente.").build();
        } catch (SQLException e) {
            context.getLogger().severe("Error SQL: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el rol.").build();
        }
    }

    @FunctionName("GetRoles")
    public HttpResponseMessage getRoles(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Obteniendo todos los roles.");

        try (Connection connection = DriverManager.getConnection("jdbc:your_database_url", "username", "password");
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM SPRING_BOOT_EXP1.ROL")) {

            List<String> roles = new ArrayList<>();
            while (resultSet.next()) {
                roles.add(resultSet.getString("NOMBRE"));
            }
            return request.createResponseBuilder(HttpStatus.OK).body(roles).build();
        } catch (SQLException e) {
            context.getLogger().severe("Error SQL: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener los roles.").build();
        }
    }

    @FunctionName("UpdateRol")
    public HttpResponseMessage updateRol(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Actualizando un rol.");

        String requestBody = request.getBody().orElse(null);
        if (requestBody == null || !requestBody.contains(":")) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Formato de solicitud inválido. Use 'id:nombre'.").build();
        }

        String[] parts = requestBody.split(":");
        int idRol = Integer.parseInt(parts[0]);
        String newName = parts[1];

        try (Connection connection = DriverManager.getConnection("jdbc:your_database_url", "username", "password");
             PreparedStatement statement = connection.prepareStatement("UPDATE SPRING_BOOT_EXP1.ROL SET NOMBRE = ? WHERE ID_ROL = ?")) {
            statement.setString(1, newName);
            statement.setInt(2, idRol);
            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated > 0) {
                return request.createResponseBuilder(HttpStatus.OK).body("Rol actualizado exitosamente.").build();
            } else {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).body("Rol no encontrado.").build();
            }
        } catch (SQLException e) {
            context.getLogger().severe("Error SQL: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el rol.").build();
        }
    }

    @FunctionName("DeleteRol")
    public HttpResponseMessage deleteRol(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Eliminando un rol.");

        String idRolStr = request.getQueryParameters().get("id");
        if (idRolStr == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("El ID del rol es obligatorio.").build();
        }

        int idRol = Integer.parseInt(idRolStr);

        try (Connection connection = DriverManager.getConnection("jdbc:your_database_url", "username", "password");
             PreparedStatement statement = connection.prepareStatement("DELETE FROM SPRING_BOOT_EXP1.ROL WHERE ID_ROL = ?")) {
            statement.setInt(1, idRol);
            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted > 0) {
                return request.createResponseBuilder(HttpStatus.OK).body("Rol eliminado exitosamente.").build();
            } else {
                return request.createResponseBuilder(HttpStatus.NOT_FOUND).body("Rol no encontrado.").build();
            }
        } catch (SQLException e) {
            context.getLogger().severe("Error SQL: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el rol.").build();
        }
    }
}
