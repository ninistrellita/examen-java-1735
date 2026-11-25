package com.carmotors.dao;

import com.carmotors.config.ConexionFactory;
import com.carmotors.modelo.AsignacionTecnico;
import com.carmotors.modelo.RepuestoUsado;
import com.carmotors.modelo.ServicioReporte;
import com.carmotors.modelo.ServicioCompletado;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Lee de MySQL las filas que usan los reportes. Solo consultas, sin agrupar: eso lo hace el servicio. */
public class ReporteDao implements FuenteReportes {

    public List<ServicioCompletado> completados(LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = """
                SELECT s.id, v.placa, s.nombre_servicio, t.nombre AS tipo, e.nombre AS estado,
                       s.fecha_recepcion, s.fecha_entrega, s.descripcion_problema
                FROM servicios s
                JOIN servicio_estados e ON e.id = s.estado_id
                LEFT JOIN vehiculos v ON v.id = s.vehiculo_id
                LEFT JOIN mantenimiento_tipos t ON t.id = s.mantenimiento_tipo_id
                WHERE e.nombre IN ('Completado', 'Facturado', 'Entregado')
                  AND s.fecha_recepcion >= ? AND s.fecha_recepcion < ?
                ORDER BY s.fecha_recepcion, s.id
                """;
        List<ServicioCompletado> resultado = new ArrayList<>();
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setObject(1, desde.atStartOfDay());
            ps.setObject(2, hasta.plusDays(1).atStartOfDay());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultado.add(new ServicioCompletado(rs.getInt("id"), rs.getString("placa"),
                            rs.getString("nombre_servicio"), rs.getString("tipo"), rs.getString("estado"),
                            rs.getObject("fecha_recepcion", LocalDateTime.class),
                            rs.getObject("fecha_entrega", LocalDateTime.class),
                            rs.getString("descripcion_problema")));
                }
            }
        }
        return List.copyOf(resultado);
    }

    @Override
    public List<ServicioReporte> servicios() throws SQLException {
        return consultarServicios(null, null);
    }

    @Override
    public List<ServicioReporte> servicios(LocalDate desde, LocalDate hasta) throws SQLException {
        return consultarServicios(desde, hasta);
    }

    private List<ServicioReporte> consultarServicios(LocalDate desde, LocalDate hasta) throws SQLException {
        boolean conPeriodo = desde != null;
        String sql = "SELECT s.id, t.nombre AS tipo, s.nombre_servicio, e.nombre AS estado, s.fecha_recepcion "
                + "FROM servicios s "
                + "LEFT JOIN mantenimiento_tipos t ON t.id = s.mantenimiento_tipo_id "
                + "LEFT JOIN servicio_estados e ON e.id = s.estado_id "
                + (conPeriodo ? "WHERE s.fecha_recepcion >= ? AND s.fecha_recepcion < ? " : "")
                + "ORDER BY s.id";
        List<ServicioReporte> lista = new ArrayList<>();
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            if (conPeriodo) {
                ps.setObject(1, desde.atStartOfDay());
                ps.setObject(2, hasta.plusDays(1).atStartOfDay());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ServicioReporte(rs.getInt("id"), rs.getString("tipo"),
                            rs.getString("nombre_servicio"), rs.getString("estado"),
                            rs.getObject("fecha_recepcion", LocalDateTime.class)));
                }
            }
        }
        return lista;
    }

    @Override
    public List<AsignacionTecnico> asignaciones() throws SQLException {
        String sql = "SELECT st.servicio_id, tc.id AS tecnico_id, tc.nombre "
                + "FROM servicios_tecnicos st "
                + "JOIN tecnicos tc ON tc.id = st.tecnico_id";
        List<AsignacionTecnico> lista = new ArrayList<>();
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new AsignacionTecnico(rs.getInt("servicio_id"), rs.getInt("tecnico_id"),
                        rs.getString("nombre")));
            }
        }
        return lista;
    }

    @Override
    public List<RepuestoUsado> repuestosUsados() throws SQLException {
        String sql = "SELECT sr.servicio_id, r.id AS repuesto_id, r.nombre, sr.cantidad_usada, "
                + "sr.precio_unitario_venta "
                + "FROM servicios_repuestos sr "
                + "JOIN repuestos r ON r.id = sr.repuesto_id";
        List<RepuestoUsado> lista = new ArrayList<>();
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new RepuestoUsado(rs.getInt("servicio_id"), rs.getInt("repuesto_id"),
                        rs.getString("nombre"), rs.getInt("cantidad_usada"),
                        rs.getBigDecimal("precio_unitario_venta")));
            }
        }
        return lista;
    }
}
