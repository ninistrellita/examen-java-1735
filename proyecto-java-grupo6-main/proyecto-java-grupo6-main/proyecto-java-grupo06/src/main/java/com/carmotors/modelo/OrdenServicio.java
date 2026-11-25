package com.carmotors.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrdenServicio {
    private int id;
    private String nombreServicio;
    private final int vehiculoId;
    private final int mantenimientoTipoId;
    private LocalDateTime fechaRecepcion;
    private LocalDateTime fechaEntrega;
    private final String descripcionProblema;
    private final BigDecimal costoManoObra;
    private int estadoId;

    public OrdenServicio(int vehiculoId, int mantenimientoTipoId, String descripcionProblema,
                         BigDecimal costoManoObra, int estadoId) {
        this.vehiculoId = vehiculoId;
        this.mantenimientoTipoId = mantenimientoTipoId;
        this.descripcionProblema = descripcionProblema;
        this.costoManoObra = costoManoObra;
        this.estadoId = estadoId;
        this.fechaRecepcion = LocalDateTime.now();
    }

    public String getNombreServicio() { return nombreServicio; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getVehiculoId() { return vehiculoId; }
    public int getMantenimientoTipoId() { return mantenimientoTipoId; }
    public LocalDateTime getFechaRecepcion() { return fechaRecepcion; }
    public void setFechaRecepcion(LocalDateTime fecha) { this.fechaRecepcion = fecha; }
    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fecha) { this.fechaEntrega = fecha; }
    public String getDescripcionProblema() { return descripcionProblema; }
    public BigDecimal getCostoManoObra() { return costoManoObra; }
    public int getEstadoId() { return estadoId; }
    public void setEstadoId(int estadoId) { this.estadoId = estadoId; }
}
