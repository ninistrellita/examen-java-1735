package com.carmotors.dao;

import com.carmotors.modelo.OrdenServicio;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/** La conexión pertenece a la transacción que abre la capa de servicio. */
public class OrdenServicioDao {
    public String correoCliente(Connection c, int vehiculo) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT c.email FROM vehiculos v JOIN clientes c ON c.id = v.cliente_id WHERE v.id = ?")) {
            p.setInt(1, vehiculo);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? r.getString("email") : null;
            }
        }
    }

    public int insertar(Connection c, OrdenServicio orden) throws SQLException {
        String sql = "INSERT INTO servicios (vehiculo_id, mantenimiento_tipo_id, fecha_recepcion, "
                + "descripcion_problema, costo_mano_obra, estado_id, nombre_servicio) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setInt(1, orden.getVehiculoId());
            p.setInt(2, orden.getMantenimientoTipoId());
            p.setObject(3, orden.getFechaRecepcion());
            p.setString(4, orden.getDescripcionProblema());
            p.setBigDecimal(5, orden.getCostoManoObra());
            p.setInt(6, orden.getEstadoId());
            p.setString(7, orden.getNombreServicio());
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (!r.next()) throw new SQLException("No se obtuvo el identificador de la orden.");
                return r.getInt(1);
            }
        }
    }

    public OrdenServicio buscar(Connection c, int id, boolean bloquear) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT * FROM servicios WHERE id = ?"
                + (bloquear ? " FOR UPDATE" : ""))) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("Orden inexistente: " + id);
                return mapear(r);
            }
        }
    }

    public List<OrdenServicio> listar(Connection c) throws SQLException {
        List<OrdenServicio> resultado = new ArrayList<>();
        try (PreparedStatement p = c.prepareStatement("SELECT * FROM servicios ORDER BY id");
             ResultSet r = p.executeQuery()) {
            while (r.next()) resultado.add(mapear(r));
        }
        return List.copyOf(resultado);
    }

    /** Cambia el estado de la orden. Si fechaEntrega no es null, también la guarda (al pasar a Entregado). */
    public void actualizarEstado(Connection c, int id, int estadoId, LocalDateTime fechaEntrega) throws SQLException {
        String sql = fechaEntrega == null
                ? "UPDATE servicios SET estado_id = ? WHERE id = ?"
                : "UPDATE servicios SET estado_id = ?, fecha_entrega = ? WHERE id = ?";
        try (PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, estadoId);
            if (fechaEntrega == null) {
                p.setInt(2, id);
            } else {
                p.setObject(2, fechaEntrega);
                p.setInt(3, id);
            }
            if (p.executeUpdate() != 1) throw new SQLException("No se actualizó la orden " + id);
        }
    }

    private OrdenServicio mapear(ResultSet r) throws SQLException {
        OrdenServicio orden = new OrdenServicio(r.getInt("vehiculo_id"), r.getInt("mantenimiento_tipo_id"),
                r.getString("descripcion_problema"), r.getBigDecimal("costo_mano_obra"), r.getInt("estado_id"));
        orden.setId(r.getInt("id"));
        orden.setNombreServicio(r.getString("nombre_servicio"));
        orden.setFechaRecepcion(r.getObject("fecha_recepcion", LocalDateTime.class));
        orden.setFechaEntrega(r.getObject("fecha_entrega", LocalDateTime.class));
        return orden;
    }
}
