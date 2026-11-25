package com.carmotors.modelo;

import java.time.LocalDateTime;
import java.math.BigDecimal;

public class ReservaRepuesto {

    public enum EstadoReserva {
        RESERVADO, DESCONTADO, CANCELADO
    }

    private int id;
    private BigDecimal precioUnitarioVenta;
    private int repuestoId;
    private int servicioId;
    private int cantidad;
    private EstadoReserva estado;
    private LocalDateTime fechaReserva;

    public ReservaRepuesto() {
    }

    public ReservaRepuesto(int repuestoId, int servicioId, int cantidad) {
        this.repuestoId = repuestoId;
        this.servicioId = servicioId;
        this.cantidad = cantidad;
        this.estado = EstadoReserva.RESERVADO;
        this.fechaReserva = LocalDateTime.now();
    }

    public BigDecimal getPrecioUnitarioVenta() { return precioUnitarioVenta; }
    public void setPrecioUnitarioVenta(BigDecimal precio) { precioUnitarioVenta = precio; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRepuestoId() { return repuestoId; }
    public void setRepuestoId(int repuestoId) { this.repuestoId = repuestoId; }

    public int getServicioId() { return servicioId; }
    public void setServicioId(int servicioId) { this.servicioId = servicioId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }

    public LocalDateTime getFechaReserva() { return fechaReserva; }
    public void setFechaReserva(LocalDateTime fechaReserva) { this.fechaReserva = fechaReserva; }

    public boolean estaActiva() {
        return estado == EstadoReserva.RESERVADO;
    }

    @Override
    public String toString() {
        return "ReservaRepuesto{id=" + id + ", repuestoId=" + repuestoId
                + ", servicioId=" + servicioId + ", cantidad=" + cantidad
                + ", estado=" + estado + "}";
    }
}