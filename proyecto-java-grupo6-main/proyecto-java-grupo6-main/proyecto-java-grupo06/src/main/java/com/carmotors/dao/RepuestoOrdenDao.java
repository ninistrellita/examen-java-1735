package com.carmotors.dao;

import com.carmotors.modelo.ReservaRepuesto;
import com.carmotors.modelo.ReservaRepuesto.EstadoReserva;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repuestos de una orden: reservas, descuento de stock y registro en servicios_repuestos.
 *
 * A diferencia de ReservaRepuestoDao y JdbcRepuestoDao, aquí cada método recibe la conexión.
 * Así el servicio de órdenes hace todo en UNA transacción: si algo falla, no queda
 * el stock descontado a medias ni una reserva sin su orden.
 */
public class RepuestoOrdenDao {

    /** Datos del repuesto que se necesitan para reservarlo. */
    public record RepuestoBloqueado(int id, String nombre, int stock, BigDecimal precioVenta) { }

    /**
     * Lee el repuesto y bloquea su fila hasta que termine la transacción (FOR UPDATE).
     * Así dos órdenes no pueden reservar al mismo tiempo las últimas unidades.
     */
    public RepuestoBloqueado bloquear(Connection c, int repuestoId) throws SQLException {
        String sql = "SELECT id, nombre, cantidad_stock, precio_venta FROM repuestos WHERE id = ? FOR UPDATE";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, repuestoId);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) {
                    throw new IllegalArgumentException("Repuesto inexistente: " + repuestoId);
                }
                return new RepuestoBloqueado(r.getInt("id"), r.getString("nombre"),
                        r.getInt("cantidad_stock"), r.getBigDecimal("precio_venta"));
            }
        }
    }

    /** Unidades de ese repuesto que ya están apartadas por otras órdenes (reservas activas). */
    public int totalReservado(Connection c, int repuestoId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(cantidad), 0) FROM reservas_repuesto "
                + "WHERE repuesto_id = ? AND estado = 'RESERVADO'";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, repuestoId);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        }
    }

    public void reservar(Connection c, int ordenId, int repuestoId, int cantidad, BigDecimal precio) throws SQLException {
        String sql = "INSERT INTO reservas_repuesto (repuesto_id, servicio_id, cantidad, estado, fecha_reserva, precio_unitario_venta) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, repuestoId);
            p.setInt(2, ordenId);
            p.setInt(3, cantidad);
            p.setString(4, EstadoReserva.RESERVADO.name());
            p.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            p.setBigDecimal(6, precio);
            p.executeUpdate();
        }
    }

    /** Todas las reservas de la orden (activas, descontadas y canceladas). */
    public List<ReservaRepuesto> reservasDe(Connection c, int ordenId) throws SQLException {
        return listar(c, "SELECT rr.*, COALESCE(rr.precio_unitario_venta, r.precio_venta) AS precio_reserva "
                + "FROM reservas_repuesto rr JOIN repuestos r ON r.id = rr.repuesto_id "
                + "WHERE rr.servicio_id = ? ORDER BY rr.id", ordenId);
    }

    /** Reservas que todavía no se han descontado. Bloquea las filas para que no se descuenten dos veces. */
    public List<ReservaRepuesto> reservasActivas(Connection c, int ordenId) throws SQLException {
        return listar(c, "SELECT rr.*, COALESCE(rr.precio_unitario_venta, r.precio_venta) AS precio_reserva "
                + "FROM reservas_repuesto rr JOIN repuestos r ON r.id = rr.repuesto_id "
                + "WHERE rr.servicio_id = ? AND rr.estado = 'RESERVADO' ORDER BY rr.id FOR UPDATE", ordenId);
    }

    /**
     * Convierte una reserva en consumo real:
     * 1. resta las unidades del stock,
     * 2. las anota en servicios_repuestos (de ahí leen la factura y el reporte de consumo),
     * 3. marca la reserva como DESCONTADO para que nunca se vuelva a descontar.
     */
    public void descontar(Connection c, ReservaRepuesto reserva) throws SQLException {
        // El "cantidad_stock >= ?" impide que el stock quede negativo
        String restar = "UPDATE repuestos SET cantidad_stock = cantidad_stock - ? WHERE id = ? AND cantidad_stock >= ?";
        try (PreparedStatement p = c.prepareStatement(restar)) {
            p.setInt(1, reserva.getCantidad());
            p.setInt(2, reserva.getRepuestoId());
            p.setInt(3, reserva.getCantidad());
            if (p.executeUpdate() != 1) {
                throw new IllegalStateException("No hay stock suficiente del repuesto " + reserva.getRepuestoId()
                        + " para descontar " + reserva.getCantidad() + " unidades.");
            }
        }

        // Si la orden ya tenía ese repuesto (dos reservas del mismo), se suman las cantidades
        String usar = "INSERT INTO servicios_repuestos (servicio_id, repuesto_id, cantidad_usada, precio_unitario_venta) "
                + "SELECT ?, id, ?, ? FROM repuestos WHERE id = ? "
                + "ON DUPLICATE KEY UPDATE cantidad_usada = cantidad_usada + ?";
        try (PreparedStatement p = c.prepareStatement(usar)) {
            p.setInt(1, reserva.getServicioId());
            p.setInt(2, reserva.getCantidad());
            p.setBigDecimal(3, reserva.getPrecioUnitarioVenta());
            p.setInt(4, reserva.getRepuestoId());
            p.setInt(5, reserva.getCantidad());
            p.executeUpdate();
        }

        try (PreparedStatement p = c.prepareStatement("UPDATE reservas_repuesto SET estado = ? WHERE id = ?")) {
            p.setString(1, EstadoReserva.DESCONTADO.name());
            p.setInt(2, reserva.getId());
            p.executeUpdate();
        }
    }

    private List<ReservaRepuesto> listar(Connection c, String sql, int ordenId) throws SQLException {
        List<ReservaRepuesto> lista = new ArrayList<>();
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, ordenId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    ReservaRepuesto reserva = new ReservaRepuesto();
                    reserva.setId(r.getInt("id"));
                    reserva.setPrecioUnitarioVenta(r.getBigDecimal("precio_reserva"));
                    reserva.setRepuestoId(r.getInt("repuesto_id"));
                    reserva.setServicioId(r.getInt("servicio_id"));
                    reserva.setCantidad(r.getInt("cantidad"));
                    reserva.setEstado(EstadoReserva.valueOf(r.getString("estado")));
                    Timestamp fecha = r.getTimestamp("fecha_reserva");
                    reserva.setFechaReserva(fecha != null ? fecha.toLocalDateTime() : null);
                    lista.add(reserva);
                }
            }
        }
        return lista;
    }
}
