package com.carmotors.dao;

import com.carmotors.modelo.Tecnico;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TecnicoDao {
    public int insertar(Connection conexion, String nombre) throws SQLException {
        String sql = "INSERT INTO tecnicos (nombre) VALUES (?)";
        try (PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, nombre);
            consulta.executeUpdate();
            try (ResultSet resultado = consulta.getGeneratedKeys()) {
                if (resultado.next()) {
                    return resultado.getInt(1);
                }
                throw new SQLException("MySQL no devolvió el ID del técnico.");
            }
        }
    }

    public List<Tecnico> listar(Connection conexion) throws SQLException {
        List<Tecnico> tecnicos = new ArrayList<>();
        String sql = "SELECT id, nombre FROM tecnicos ORDER BY nombre, id";
        try (PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultados = consulta.executeQuery()) {
            while (resultados.next()) {
                tecnicos.add(new Tecnico(resultados.getInt("id"), resultados.getString("nombre")));
            }
        }
        return tecnicos;
    }
}