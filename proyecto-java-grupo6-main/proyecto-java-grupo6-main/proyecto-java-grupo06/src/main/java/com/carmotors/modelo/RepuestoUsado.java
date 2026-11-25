package com.carmotors.modelo;

import java.math.BigDecimal;
import java.util.Objects;

/** Un repuesto usado en una orden de servicio (fila de servicios_repuestos). */
public class RepuestoUsado {
    private final int servicioId;
    private final int repuestoId;
    private final String repuesto;
    private final int cantidad;
    private final BigDecimal precioUnitario;

    public RepuestoUsado(int servicioId, int repuestoId, String repuesto, int cantidad, BigDecimal precioUnitario) {
        this.servicioId = servicioId;
        this.repuestoId = repuestoId;
        this.repuesto = repuesto;
        this.cantidad = cantidad;
        this.precioUnitario = Objects.requireNonNull(precioUnitario);
    }

    public int getServicioId() { return servicioId; }
    public int getRepuestoId() { return repuestoId; }
    public String getRepuesto() { return repuesto; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
