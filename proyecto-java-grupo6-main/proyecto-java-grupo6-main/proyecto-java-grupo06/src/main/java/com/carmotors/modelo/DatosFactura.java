package com.carmotors.modelo;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datos que el módulo de reportes le entrega a facturación.
 * Facturación usa estos datos para calcular los impuestos y el total final.
 */
public class DatosFactura {
    private int servicioId;          // número de la orden
    private String estado;           // estado de la orden, por ejemplo "Completado"
    private String cliente;          // nombre del cliente
    private String documento;        // documento del cliente
    private String placa;            // placa del vehículo
    private BigDecimal manoObra;     // costo de la mano de obra
    private List<RepuestoUsado> repuestos; // repuestos que se usaron en la orden

    public DatosFactura(int servicioId, String estado, String cliente, String documento,
                        String placa, BigDecimal manoObra, List<RepuestoUsado> repuestos) {
        this.servicioId = servicioId;
        this.estado = estado;
        this.cliente = cliente;
        this.documento = documento;
        this.placa = placa;
        this.manoObra = manoObra;
        this.repuestos = repuestos;
    }

    public int getServicioId() { return servicioId; }
    public String getEstado() { return estado; }
    public String getCliente() { return cliente; }
    public String getDocumento() { return documento; }
    public String getPlaca() { return placa; }
    public BigDecimal getManoObra() { return manoObra; }
    public List<RepuestoUsado> getRepuestos() { return repuestos; }

    /** Solo se factura una orden que está "Completado" (terminada y todavía sin factura). */
    public boolean sePuedeFacturar() {
        return "Completado".equals(estado);
    }

    /** Suma de lo que costaron los repuestos: cantidad x precio de cada uno. */
    public BigDecimal getTotalRepuestos() {
        BigDecimal total = BigDecimal.ZERO;
        for (RepuestoUsado repuesto : repuestos) {
            total = total.add(repuesto.getSubtotal());
        }
        return total;
    }

    /** Mano de obra + repuestos (todavía sin impuestos). */
    public BigDecimal getSubtotal() {
        return manoObra.add(getTotalRepuestos());
    }

    @Override
    public String toString() {
        return "Orden " + servicioId + " | " + cliente + " | " + placa
                + " | repuestos $" + getTotalRepuestos()
                + " + mano de obra $" + manoObra
                + " = $" + getSubtotal();
    }
}
