package com.function;

import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RolDataFetcher {
    /*{
    "query": "mutation CreateUserRole($idUser: ID!, $idRol: ID!) { createUserRole(idUser: $idUser, idRol: $idRol) }",
    "variables": {
        "idUser": "1",
        "idRol": "2"
    }
    }
    */
    public DataFetcher<String> createUserRole() {
        return environment -> {
            String idString = environment.getArgument("idUser");
            String idString2 = environment.getArgument("idRol");
            Integer idUser;
            Integer idRol;
            
            try {
                idUser = Integer.parseInt(idString);
                idRol = Integer.parseInt(idString2);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("ID de usuario o rol no válido: " + e.getMessage());
            }

            try (Connection conn = OracleDBConnection.getConnection()) {
                String sql = "INSERT INTO USUARIO_ROL (ID_USUARIO, ID_ROL) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, idUser);
                    stmt.setInt(2, idRol);
                    System.out.println("Ejecutando la consulta: " + idUser + "," + idRol); // Debugging line
                    stmt.executeUpdate();
                    return "Relación usuario-rol creada exitosamente.";
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al crear la relación usuario-rol: " + e.getMessage());
            }
        };
    }
   
    /*
    {
    "query": "query GetUserRoles($idUser: ID!) { getUserRoles(idUser: $idUser) { idRol nombreRol } }",
    "variables": {
        "idUser": "1"
    }
    }
     */
    public DataFetcher<List<Map<String, Object>>> getUserRoles() {
        return environment -> {
            String idString = environment.getArgument("idUser");
            Integer idUser;

            try {
                idUser = Integer.parseInt(idString); // Convertir el id de String a Integer
                System.out.println("ID proporcionado: " + idUser); // Debugging line
            } catch (NumberFormatException e) {
                throw new RuntimeException("El id proporcionado no es un número válido: " + idString);
            }
            List<Map<String, Object>> roles = new ArrayList<>();

            try (Connection conn = OracleDBConnection.getConnection()) {
                String sql = "SELECT C.ID_ROL AS ID_ROL, C.NOMBRE AS NOMBRE_ROL FROM USUARIO A, USUARIO_ROL B, ROL C WHERE A.ID_USUARIO = B.ID_USUARIO AND B.ID_ROL = C.ID_ROL AND B.ID_USUARIO = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, idUser);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> role = new HashMap<>();
                            role.put("idRol", rs.getInt("ID_ROL"));
                            role.put("nombreRol", rs.getString("NOMBRE_ROL"));
                            roles.add(role);
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al obtener las relaciones usuario-rol: " + e.getMessage());
            }
            return roles;
        };
    }
     /*
      {
        "query": "mutation UpdateUserRole($idUser: ID!, $oldIdRol: ID!, $newIdRol: ID!) { updateUserRole(idUser: $idUser, oldIdRol: $oldIdRol, newIdRol: $newIdRol) }",
        "variables": {
            "idUser": "1",
            "oldIdRol": "2",
            "newIdRol": "3"
        }
     }
      */
    public DataFetcher<String> updateUserRole() {
        return environment -> {
            String idString = environment.getArgument("idUser");
            String idString2 = environment.getArgument("oldIdRol");
            String idString3 = environment.getArgument("newIdRol");
            Integer idUser;
            Integer oldIdRol;
            Integer newIdRol;

            try {
                idUser = Integer.parseInt(idString); // Convertir el id de String a Integer
                oldIdRol = Integer.parseInt(idString2); // Convertir el idRol de String a Integer
                newIdRol = Integer.parseInt(idString3); // Convertir el idRolNuevo de String a Integer
                System.out.println("ID proporcionado: " + idUser); // Debugging line
            } catch (NumberFormatException e) {
                throw new RuntimeException("El id proporcionado no es un número válido: " + idString);
            }

            try (Connection conn = OracleDBConnection.getConnection()) {
                String sql = "UPDATE USUARIO_ROL SET ID_ROL = ? WHERE ID_USUARIO = ? AND ID_ROL = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, newIdRol);
                    stmt.setInt(2, idUser);
                    stmt.setInt(3, oldIdRol);
                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        return "Relación usuario-rol actualizada exitosamente.";
                    } else {
                        return "Relación usuario-rol no encontrada.";
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al actualizar la relación usuario-rol: " + e.getMessage());
            }
        };
    }
  /*
    {
        "query": "mutation DeleteUserRole($idUser: ID!, $idRol: ID!) { deleteUserRole(idUser: $idUser, idRol: $idRol) }",
        "variables": {
            "idUser": "1",
            "idRol": "3"
        }
    }
   */

    public DataFetcher<String> deleteUserRole() {
        return environment -> {
            String idString = environment.getArgument("idUser");
            String idString2 = environment.getArgument("idRol");
            Integer idUser;
            Integer idRol;
            try {
                idUser = Integer.parseInt(idString); // Convertir el id de String a Integer
                idRol = Integer.parseInt(idString2); // Convertir el idRol de String a Integer
                System.out.println("ID proporcionado: " + idUser); // Debugging line
            } catch (NumberFormatException e) {
                throw new RuntimeException("El id proporcionado no es un número válido: " + idString);
            }
            try (Connection conn = OracleDBConnection.getConnection()) {
                String sql = "DELETE FROM USUARIO_ROL WHERE ID_USUARIO = ? AND ID_ROL = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, idUser);
                    stmt.setInt(2, idRol);
                    int rowsDeleted = stmt.executeUpdate();
                    if (rowsDeleted > 0) {
                        return "Relación usuario-rol eliminada exitosamente.";
                    } else {
                        return "Relación usuario-rol no encontrada.";
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al eliminar la relación usuario-rol: " + e.getMessage());
            }
        };
    }
}
