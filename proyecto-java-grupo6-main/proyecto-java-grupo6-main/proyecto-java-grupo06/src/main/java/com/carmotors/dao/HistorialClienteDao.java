package com.carmotors.dao;

import com.carmotors.config.ConexionFactory;
import com.carmotors.modelo.HistorialVehiculo;
import com.carmotors.modelo.HistorialVehiculo.Visita;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HistorialClienteDao {
    public List<HistorialVehiculo> buscar(String documento) throws SQLException {
        String sql = "SELECT c.nombre, c.email, v.placa, v.marca, v.modelo, s.id AS servicio_id, "
                + "DATE(s.fecha_recepcion) AS recepcion, DATE(s.fecha_entrega) AS entrega, "
                + "t.nombre AS tipo, e.nombre AS estado, s.descripcion_problema "
                + "FROM clientes c "
                + "JOIN vehiculos v ON v.cliente_id = c.id "
                + "LEFT JOIN servicios s ON s.vehiculo_id = v.id "
                + "LEFT JOIN mantenimiento_tipos t ON t.id = s.mantenimiento_tipo_id "
                + "LEFT JOIN servicio_estados e ON e.id = s.estado_id "
                + "WHERE (? IS NULL OR c.documento_identidad = ?) "
                + "ORDER BY c.nombre, v.placa, s.fecha_recepcion DESC, s.id DESC ";
        Map<String, HistorialVehiculo> historial = new LinkedHashMap<String, HistorialVehiculo>();
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, documento);
            consulta.setString(2, documento);
            try (ResultSet filas = consulta.executeQuery()) {
                while (filas.next()) {
                    String placa = filas.getString("placa");
                    if (!historial.containsKey(placa)) {
                        historial.put(placa, new HistorialVehiculo(filas.getString("nombre"),
                                filas.getString("email"), placa, filas.getString("marca"),
                                filas.getString("modelo"), new ArrayList<>()));
                    }
                    if (filas.getObject("servicio_id") != null) {
                        historial.get(placa).getVisitas().add(new Visita(
                                filas.getObject("recepcion", LocalDate.class),
                                filas.getObject("entrega", LocalDate.class),
                                filas.getString("tipo"), filas.getString("estado"),
                                filas.getString("descripcion_problema")));
                    }
                }
            }
        }
        return List.copyOf(historial.values());
    }
}
