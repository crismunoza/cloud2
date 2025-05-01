package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.EventGridTrigger;


import org.json.JSONObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EventGridConsumerFunction {

    // Este método se ejecutará cuando Event Grid publique un evento
    @FunctionName("EventGridUserEventReceiver")
    public void run(
        @EventGridTrigger(name = "event") String event, 
        final ExecutionContext context) {
        
        // Log para ver el evento recibido
        context.getLogger().info("Evento recibido: " + event);
       System.out.println("Evento recibido: " + event);
        try {
            // Parsear el evento JSON
            JSONObject eventJson = new JSONObject(event);
            String eventType = eventJson.getString("eventType");
            JSONObject eventData = eventJson.getJSONObject("data");
            
            context.getLogger().info("Tipo de Evento: " + eventType);
            context.getLogger().info("Datos del Evento: " + eventData.toString());
            System.out.println("Tipo de Evento: " + eventType);
            System.out.println("Datos del Evento: " + eventData.toString());
            switch (eventType) {
                case "UserCreated":
                    context.getLogger().info("Creando usuario...");
                    try (Connection conn = OracleDBConnection.getConnection()) {
                        String sql = "INSERT INTO USUARIO (NOMBRE, EMAIL, PASSWORD, ESTADO) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, eventData.getString("nombre"));
                            stmt.setString(2, eventData.getString("email"));
                            stmt.setString(3, eventData.getString("password"));
                            stmt.setInt(4, 1); 
                            stmt.executeUpdate();
                        }
                    } catch (SQLException e) {
                        context.getLogger().severe("Error al crear usuario: " + e.getMessage());
                    }
                    break;
                case "UserUpdated":
                    context.getLogger().info("Actualizando usuario...");
                    try (Connection conn = OracleDBConnection.getConnection()) {
                        String sql = "UPDATE USUARIO SET NOMBRE = ?, EMAIL = ?, PASSWORD = ? WHERE ID_USUARIO = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, eventData.getString("nombre"));
                            stmt.setString(2, eventData.getString("email"));
                            stmt.setString(3, eventData.getString("password"));
                            stmt.setInt(4, eventData.getInt("id"));
                            stmt.executeUpdate();
                        }
                    } catch (SQLException e) {
                        context.getLogger().severe("Error al actualizar usuario: " + e.getMessage());
                    }
                    break;
                case "UserDeleted":
                    context.getLogger().info("Eliminando usuario...");
                    try (Connection conn = OracleDBConnection.getConnection()) {
                        String sql = "DELETE FROM USUARIO WHERE ID_USUARIO = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setInt(1, eventData.getInt("id"));
                            stmt.executeUpdate();
                        }
                    } catch (SQLException e) {
                        context.getLogger().severe("Error al eliminar usuario: " + e.getMessage());
                    }
                    break;
                case "RoleCreated":
                    context.getLogger().info("Creando Rol...");
                    try (Connection conn = OracleDBConnection.getConnection()) {
                        String sql = "INSERT INTO USUARIO_ROL (ID_USUARIO, ID_ROL) VALUES (?, ?)";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setInt(1, eventData.getInt("idUser"));
                            stmt.setInt(2, eventData.getInt("idRol"));
                            stmt.executeUpdate();
                        }
                    } catch (SQLException e) {
                        context.getLogger().severe("Error al crear Rol: " + e.getMessage());
                    }
                    break;
                case "RoleUpdated":
                    context.getLogger().info("Actualizando Rol...");
                    try (Connection conn = OracleDBConnection.getConnection()) {
                        String sql = "UPDATE USUARIO_ROL SET ID_ROL = ? WHERE ID_USUARIO = ? AND ID_ROL = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setInt(1, eventData.getInt("newIdRol"));
                            stmt.setInt(2, eventData.getInt("idUser"));
                            stmt.setInt(3, eventData.getInt("idRol"));
                            stmt.executeUpdate();
                        }
                    } catch (SQLException e) {
                        context.getLogger().severe("Error al actualizar Rol: " + e.getMessage());
                    }
                break;
                case "RoleDeleted":
                    context.getLogger().info("Eliminando Rol...");
                    try (Connection conn = OracleDBConnection.getConnection()) {
                        String sql = "DELETE FROM USUARIO_ROL WHERE ID_USUARIO = ? AND ID_ROL = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setInt(1, eventData.getInt("idUser"));
                            stmt.setInt(2, eventData.getInt("idRol"));
                            stmt.executeUpdate();
                        }
                    } catch (SQLException e) {
                        context.getLogger().severe("Error al eliminar Rol: " + e.getMessage());
                    }
                    break;                
                default:
                    context.getLogger().warning("Tipo de evento no reconocido: " + eventType);
                    System.out.println("Tipo de evento no reconocido: " + eventType);
                    break;
            }
        } catch (Exception e) {
            context.getLogger().severe("Error al procesar el evento: " + e.getMessage());
        }
    }
}
