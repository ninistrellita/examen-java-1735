package com.carmotors.dao;

import com.carmotors.config.ConexionFactory;
import com.carmotors.modelo.Cliente;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClienteDao {
    public void insertar(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO clientes (nombre, documento_identidad, telefono, email) VALUES (?, ?, ?, ?)";
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, cliente.getNombre());
            consulta.setString(2, cliente.getDocumento());
            consulta.setString(3, cliente.getTelefono());
            consulta.setString(4, cliente.getEmail());
            consulta.executeUpdate();
        }
    }
}
