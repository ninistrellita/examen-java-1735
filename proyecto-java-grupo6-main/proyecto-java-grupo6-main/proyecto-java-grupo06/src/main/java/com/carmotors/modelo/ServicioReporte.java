package com.carmotors.modelo;

import java.time.LocalDateTime;
import java.util.Objects;

/** Una orden de servicio tal como la necesitan los reportes. */
public class ServicioReporte {
    private final int id;
    private final String tipo;
    private final String nombreServicio;
    private final String estado;
    private final LocalDateTime fechaRecepcion;

    /** Para órdenes sin nombre de servicio (las creadas antes de que existiera la lista). */
    public ServicioReporte(int id, String tipo, String estado, LocalDateTime fechaRecepcion) {
        this(id, tipo, null, estado, fechaRecepcion);
    }

    /** nombreServicio puede ser null: el reporte la cuenta como "Sin nombre". */
    public ServicioReporte(int id, String tipo, String nombreServicio, String estado,
                           LocalDateTime fechaRecepcion) {
        this.id = id;
        this.tipo = Objects.requireNonNullElse(tipo, "Sin tipo");
        this.nombreServicio = nombreServicio;
        this.estado = Objects.requireNonNullElse(estado, "Sin estado");
        this.fechaRecepcion = Objects.requireNonNull(fechaRecepcion);
    }

    public int getId() { return id; }
    public String getTipo() { return tipo; }
    public String getNombreServicio() { return nombreServicio; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaRecepcion() { return fechaRecepcion; }
}
