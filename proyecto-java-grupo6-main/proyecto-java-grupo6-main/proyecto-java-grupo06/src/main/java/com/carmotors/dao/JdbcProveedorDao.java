package com.carmotors.dao;

import com.carmotors.config.ConexionFactory;
import com.carmotors.modelo.Proveedor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class JdbcProveedorDao {

    public int insertar(Proveedor proveedor) throws SQLException {
        String sql = "INSERT INTO proveedores (nombre, contacto) VALUES (?, ?)";
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getContacto());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    proveedor.setId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    public void actualizar(Proveedor proveedor) throws SQLException {
        String sql = "UPDATE proveedores SET nombre = ?, contacto = ? WHERE id = ?";
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, proveedor.getNombre());
            ps.setString(2, proveedor.getContacto());
            ps.setInt(3, proveedor.getId());
            ps.executeUpdate();
        }
    }

    public Optional<Proveedor> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, contacto FROM proveedores WHERE id = ?";
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Proveedor> buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT id, nombre, contacto FROM proveedores WHERE nombre = ?";
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public List<Proveedor> listarTodos() throws SQLException {
        String sql = "SELECT id, nombre, contacto FROM proveedores ORDER BY nombre";
        List<Proveedor> lista = new ArrayList<>();
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setId(rs.getInt("id"));
        p.setNombre(rs.getString("nombre"));
        p.setContacto(rs.getString("contacto"));
        return p;
    }
}
