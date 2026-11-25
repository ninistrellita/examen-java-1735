package com.carmotors.modelo;

import java.math.BigDecimal;

/** Resultado del reporte de consumo: unidades y valor usados de un repuesto. */
public class ConsumoRepuesto {
    private final int repuestoId;
    private final String repuesto;
    private final long unidades;
    private final BigDecimal valorTotal;
    private final long ordenes;

    public ConsumoRepuesto(int repuestoId, String repuesto, long unidades, BigDecimal valorTotal, long ordenes) {
        this.repuestoId = repuestoId;
        this.repuesto = repuesto;
        this.unidades = unidades;
        this.valorTotal = valorTotal;
        this.ordenes = ordenes;
    }

    public int getRepuestoId() { return repuestoId; }
    public String getRepuesto() { return repuesto; }
    public long getUnidades() { return unidades; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public long getOrdenes() { return ordenes; }

    @Override
    public String toString() {
        return repuesto + ": " + unidades + " u. en " + ordenes + " orden(es), $" + valorTotal;
    }
}
