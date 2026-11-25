package com.carmotors.dao;

import com.carmotors.modelo.AsignacionTecnico;
import com.carmotors.modelo.RepuestoUsado;
import com.carmotors.modelo.ServicioReporte;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Datos crudos para los reportes. Los cálculos se hacen con Streams en ReporteServicio;
 * esta interfaz permite probarlos con datos en memoria, sin MySQL.
 */
public interface FuenteReportes {
    List<ServicioReporte> servicios() throws SQLException;

    default List<ServicioReporte> servicios(LocalDate desde, LocalDate hasta) throws SQLException {
        return servicios().stream()
                .filter(s -> {
                    LocalDate dia = s.getFechaRecepcion().toLocalDate();
                    return !dia.isBefore(desde) && !dia.isAfter(hasta);
                })
                .toList();
    }

    List<AsignacionTecnico> asignaciones() throws SQLException;

    List<RepuestoUsado> repuestosUsados() throws SQLException;
}
