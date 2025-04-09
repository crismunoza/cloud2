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
public class UsuarioDataFetcher {
    /* 
     {
        "query": "mutation { createUser(nombre: \"DIGNA\", email: \"crismuno@hotmail\", password: \"sadasda\") }"
      }
    */
    public DataFetcher<String> createUser() {
        return environment -> {
            String nombre = environment.getArgument("nombre");
            String email = environment.getArgument("email");
            String password = environment.getArgument("password");

            if (nombre == null || email == null || password == null) {
                throw new RuntimeException("Faltan campos requeridos: nombre, email o contraseña.");
            }

            try (Connection conn = OracleDBConnection.getConnection()) {
                String sql = "INSERT INTO USUARIO (NOMBRE, EMAIL, PASSWORD) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    System.out.println("SQL: " + sql); // Debugging line
                    stmt.setString(1, nombre);
                    stmt.setString(2, email);
                    stmt.setString(3, password);
                    System.out.println("Se va a ejecutar la inserción: " + nombre + ", " + email + "," + password); // Debugging line
                    stmt.executeUpdate();
                    return "Usuario creado exitosamente.";
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al crear el usuario: " + e.getMessage());
            }
        };
    }
    /* 
    {
    "query": "{ getUser(id: 3) { id nombre email estado } }"
    }
    */

    public DataFetcher<Map<String, Object>> getUser() {
        return environment -> {
            String idString = environment.getArgument("id");
            Integer id;

            try {
                id = Integer.parseInt(idString); // Convertir el id de String a Integer
                System.out.println("ID proporcionado: " + id); // Debugging line
            } catch (NumberFormatException e) {
                throw new RuntimeException("El id proporcionado no es un número válido: " + idString);
            }

            try (Connection conn = OracleDBConnection.getConnection()) {
                String sql = "SELECT * FROM USUARIO WHERE ID_USUARIO = ?";
                System.out.println("SQL: " + sql); // Debugging line
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    System.out.println("Se va a pasar el ID a la consulta: " + id);
                    stmt.setInt(1, id);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Map<String, Object> user = new HashMap<>();
                            user.put("id", rs.getInt("ID_USUARIO"));
                            user.put("nombre", rs.getString("NOMBRE"));
                            user.put("email", rs.getString("EMAIL"));
                            user.put("estado", rs.getInt("ESTADO"));
                            System.out.println("Usuario encontrado: " + user); // Debugging line
                            return user;
                        } else {
                            throw new RuntimeException("Usuario con id " + id + " no encontrado.");
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al recuperar el usuario: " + e.getMessage());
            }
        };
    }
    
    /*
       {
        "query": "mutation { updateUser(id: 3, nombre: \"eduardo\", email: \"nuevo.email@example.com\", password: \"nuevaContraseña123\") }"
        }

     */

    public DataFetcher<String> updateUser() {
        return environment -> {
            String idString = environment.getArgument("id");
            Integer id;
            String nombre = environment.getArgument("nombre");
            String email = environment.getArgument("email");
            String password = environment.getArgument("password");
            Integer estado = environment.getArgument("estado");
    
            try {
                id = Integer.parseInt(idString); // Convertir el id de String a Integer
                System.out.println("ID proporcionado: " + id); // Debugging line
            } catch (NumberFormatException e) {
                throw new RuntimeException("El id proporcionado no es un número válido: " + idString);
            }
    
            // Construir la consulta SQL de manera dinámica
            StringBuilder sql = new StringBuilder("UPDATE USUARIO SET ");
            List<Object> parameters = new ArrayList<>();
    
            if (nombre != null) {
                sql.append("NOMBRE = ?, ");
                parameters.add(nombre);
            }
            if (email != null) {
                sql.append("EMAIL = ?, ");
                parameters.add(email);
            }
            if (password != null) {
                sql.append("PASSWORD = ?, ");
                parameters.add(password);
            }
            if (estado != null) {
                sql.append("ESTADO = ?, ");
                parameters.add(estado);
            }
    
            // Eliminar la última coma y espacio
            if (sql.toString().endsWith(", ")) {
                sql.setLength(sql.length() - 2);
            }
    
            sql.append(" WHERE ID_USUARIO = ?");
            parameters.add(id);
    
            try (Connection conn = OracleDBConnection.getConnection()) {
                try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                    // Establecer los parámetros en la consulta
                    for (int i = 0; i < parameters.size(); i++) {
                        stmt.setObject(i + 1, parameters.get(i));
                    }
    
                    int rowsUpdated = stmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        return "Usuario actualizado exitosamente.";
                    } else {
                        return "Usuario no encontrado.";
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al actualizar el usuario: " + e.getMessage());
            }
        };
    }
    /* 
    {
    "query": "mutation DeleteUser($id: ID!) { deleteUser(id: $id) }",
    "variables": {
        "id": "3"}
    }
    */
    public DataFetcher<String> deleteUser() {
        return environment -> {
            String idString = environment.getArgument("id");
            Integer id;
    
            try {
                id = Integer.parseInt(idString); // Convertir el id de String a Integer
                System.out.println("ID proporcionado: " + id); // Debugging line
            } catch (NumberFormatException e) {
                throw new RuntimeException("El id proporcionado no es un número válido: " + idString);
            }
    
            try (Connection conn = OracleDBConnection.getConnection()) {
                String sql = "DELETE FROM USUARIO WHERE ID_USUARIO = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, id);
                    int rowsDeleted = stmt.executeUpdate();
                    if (rowsDeleted > 0) {
                        return "Usuario eliminado exitosamente.";
                    } else {
                        return "Usuario no encontrado.";
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al eliminar el usuario: " + e.getMessage());
            }
        };
    }
    /* 
    {
        "query": "{ listUsers { id nombre email estado } }"
    }
    */
    public DataFetcher<List<Map<String, Object>>> listUsers() {
        return environment -> {
            List<Map<String, Object>> users = new ArrayList<>();
            try (Connection conn = OracleDBConnection.getConnection()) {
                String sql = "SELECT * FROM USUARIO";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> user = new HashMap<>();
                        user.put("id", rs.getInt("ID_USUARIO"));
                        user.put("nombre", rs.getString("NOMBRE"));
                        user.put("email", rs.getString("EMAIL"));
                        user.put("estado", rs.getInt("ESTADO"));
                        users.add(user);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al listar usuarios: " + e.getMessage());
            }
            return users;
        };
    }
}
