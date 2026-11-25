package com.carmotors.dao;

import com.carmotors.config.ConexionFactory;
import com.carmotors.modelo.Lote;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoteDao {

    public int insertar(Lote lote) throws SQLException {
        String sql = "INSERT INTO lotes (codigo_lote, repuesto_id, proveedor_id, fecha_ingreso, "
                + "fecha_caducidad, cantidad_recibida, cantidad_disponible) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, lote.getCodigoLote());
            ps.setInt(2, lote.getRepuestoId());
            ps.setInt(3, lote.getProveedorId());
            ps.setDate(4, Date.valueOf(lote.getFechaIngreso()));
            if (lote.getFechaCaducidad() != null) {
                ps.setDate(5, Date.valueOf(lote.getFechaCaducidad()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setInt(6, lote.getCantidadRecibida());
            ps.setInt(7, lote.getCantidadDisponible());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    lote.setId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    public Optional<Lote> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, codigo_lote, repuesto_id, proveedor_id, fecha_ingreso, "
                + "fecha_caducidad, cantidad_recibida, cantidad_disponible FROM lotes WHERE id = ?";
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Lote> buscarPorCodigo(String codigoLote) throws SQLException {
        String sql = "SELECT id, codigo_lote, repuesto_id, proveedor_id, fecha_ingreso, "
                + "fecha_caducidad, cantidad_recibida, cantidad_disponible FROM lotes WHERE codigo_lote = ?";
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, codigoLote);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public List<Lote> listarPorRepuesto(int repuestoId) throws SQLException {
        String sql = "SELECT id, codigo_lote, repuesto_id, proveedor_id, fecha_ingreso, "
                + "fecha_caducidad, cantidad_recibida, cantidad_disponible "
                + "FROM lotes WHERE repuesto_id = ? ORDER BY fecha_ingreso DESC";
        return listar(sql, repuestoId);
    }

    public List<Lote> listarPorProveedor(int proveedorId) throws SQLException {
        String sql = "SELECT id, codigo_lote, repuesto_id, proveedor_id, fecha_ingreso, "
                + "fecha_caducidad, cantidad_recibida, cantidad_disponible "
                + "FROM lotes WHERE proveedor_id = ? ORDER BY fecha_ingreso DESC";
        return listar(sql, proveedorId);
    }

    public List<Lote> listarProximosACaducar() throws SQLException {
        String sql = "SELECT id, codigo_lote, repuesto_id, proveedor_id, fecha_ingreso, "
                + "fecha_caducidad, cantidad_recibida, cantidad_disponible FROM lotes "
                + "WHERE fecha_caducidad IS NOT NULL AND fecha_caducidad <= DATE_ADD(CURDATE(), INTERVAL 30 DAY) "
                + "ORDER BY fecha_caducidad";
        List<Lote> lista = new ArrayList<>();
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    private List<Lote> listar(String sql, int id) throws SQLException {
        List<Lote> lista = new ArrayList<>();
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Lote mapear(ResultSet rs) throws SQLException {
        Lote lote = new Lote();
        lote.setId(rs.getInt("id"));
        lote.setCodigoLote(rs.getString("codigo_lote"));
        lote.setRepuestoId(rs.getInt("repuesto_id"));
        lote.setProveedorId(rs.getInt("proveedor_id"));
        Date ingreso = rs.getDate("fecha_ingreso");
        lote.setFechaIngreso(ingreso != null ? ingreso.toLocalDate() : null);
        Date caducidad = rs.getDate("fecha_caducidad");
        lote.setFechaCaducidad(caducidad != null ? caducidad.toLocalDate() : null);
        lote.setCantidadRecibida(rs.getInt("cantidad_recibida"));
        lote.setCantidadDisponible(rs.getInt("cantidad_disponible"));
        return lote;
    }
}