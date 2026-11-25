package com.carmotors.dao;

import com.carmotors.config.ConexionFactory;
import com.carmotors.modelo.EvaluacionProveedor;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EvaluacionProveedorDao {

    public int insertar(EvaluacionProveedor evaluacion) throws SQLException {
        String sql = "INSERT INTO evaluaciones_proveedor "
                + "(proveedor_id, fecha_evaluacion, calificacion_calidad, calificacion_puntualidad, observaciones) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, evaluacion.getProveedorId());
            ps.setDate(2, Date.valueOf(evaluacion.getFechaEvaluacion()));
            ps.setInt(3, evaluacion.getCalificacionCalidad());
            ps.setInt(4, evaluacion.getCalificacionPuntualidad());
            ps.setString(5, evaluacion.getObservaciones());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    evaluacion.setId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    public List<EvaluacionProveedor> listarPorProveedor(int proveedorId) throws SQLException {
        String sql = "SELECT id, proveedor_id, fecha_evaluacion, calificacion_calidad, "
                + "calificacion_puntualidad, observaciones FROM evaluaciones_proveedor "
                + "WHERE proveedor_id = ? ORDER BY fecha_evaluacion DESC";
        List<EvaluacionProveedor> lista = new ArrayList<>();
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, proveedorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<EvaluacionProveedor> buscarUltimaPorProveedor(int proveedorId) throws SQLException {
        String sql = "SELECT id, proveedor_id, fecha_evaluacion, calificacion_calidad, "
                + "calificacion_puntualidad, observaciones FROM evaluaciones_proveedor "
                + "WHERE proveedor_id = ? ORDER BY fecha_evaluacion DESC LIMIT 1";
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, proveedorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    private EvaluacionProveedor mapear(ResultSet rs) throws SQLException {
        EvaluacionProveedor e = new EvaluacionProveedor();
        e.setId(rs.getInt("id"));
        e.setProveedorId(rs.getInt("proveedor_id"));
        Date fecha = rs.getDate("fecha_evaluacion");
        e.setFechaEvaluacion(fecha != null ? fecha.toLocalDate() : null);
        e.setCalificacionCalidad(rs.getInt("calificacion_calidad"));
        e.setCalificacionPuntualidad(rs.getInt("calificacion_puntualidad"));
        e.setObservaciones(rs.getString("observaciones"));
        return e;
    }
}