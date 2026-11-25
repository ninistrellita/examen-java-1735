package com.carmotors.dao;

import com.carmotors.config.ConexionFactory;
import com.carmotors.modelo.ReservaRepuesto;
import com.carmotors.modelo.ReservaRepuesto.EstadoReserva;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** DAO JDBC de reservas de repuestos. Día 3 - Yeimer */
public class ReservaRepuestoDao {

    public int insertar(ReservaRepuesto reserva) throws SQLException {
        String sql = "INSERT INTO reservas_repuesto (repuesto_id, servicio_id, cantidad, estado, fecha_reserva) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reserva.getRepuestoId());
            ps.setInt(2, reserva.getServicioId());
            ps.setInt(3, reserva.getCantidad());
            ps.setString(4, reserva.getEstado().name());
            ps.setTimestamp(5, Timestamp.valueOf(reserva.getFechaReserva()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    reserva.setId(id);
                    return id;
                }
            }
        }
        return 0;
    }

    public void actualizarEstado(int reservaId, EstadoReserva estado) throws SQLException {
        String sql = "UPDATE reservas_repuesto SET estado = ? WHERE id = ?";
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, reservaId);
            ps.executeUpdate();
        }
    }

    public List<ReservaRepuesto> listarPorServicio(int servicioId) throws SQLException {
        String sql = "SELECT id, repuesto_id, servicio_id, cantidad, estado, fecha_reserva "
                + "FROM reservas_repuesto WHERE servicio_id = ? ORDER BY id";
        List<ReservaRepuesto> lista = new ArrayList<>();
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, servicioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<ReservaRepuesto> listarActivasPorRepuesto(int repuestoId) throws SQLException {
        String sql = "SELECT id, repuesto_id, servicio_id, cantidad, estado, fecha_reserva "
                + "FROM reservas_repuesto WHERE repuesto_id = ? AND estado = 'RESERVADO'";
        List<ReservaRepuesto> lista = new ArrayList<>();
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, repuestoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private ReservaRepuesto mapear(ResultSet rs) throws SQLException {
        ReservaRepuesto r = new ReservaRepuesto();
        r.setId(rs.getInt("id"));
        r.setRepuestoId(rs.getInt("repuesto_id"));
        r.setServicioId(rs.getInt("servicio_id"));
        r.setCantidad(rs.getInt("cantidad"));
        r.setEstado(EstadoReserva.valueOf(rs.getString("estado")));
        Timestamp ts = rs.getTimestamp("fecha_reserva");
        r.setFechaReserva(ts != null ? ts.toLocalDateTime() : null);
        return r;
    }
}
