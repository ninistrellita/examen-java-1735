package com.carmotors.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Factura de una orden: los datos de la orden más los impuestos y el total final.
 * Se guarda en la tabla facturas y se imprime en un archivo .txt.
 */
public class Factura {
    /** IVA de Colombia. Si el grupo acuerda otra tasa, solo se cambia aquí. */
    public static final BigDecimal TASA_IVA = new BigDecimal("0.19");

    private final int numero;                 // id de la tabla facturas; 0 si todavía no se ha guardado
    private final LocalDateTime fechaEmision;
    private final DatosFactura datos;
    private final BigDecimal impuestos;
    private final BigDecimal total;

    public Factura(int numero, LocalDateTime fechaEmision, DatosFactura datos,
                   BigDecimal impuestos, BigDecimal total) {
        this.numero = numero;
        this.fechaEmision = Objects.requireNonNull(fechaEmision);
        this.datos = Objects.requireNonNull(datos);
        this.impuestos = Objects.requireNonNull(impuestos);
        this.total = Objects.requireNonNull(total);
    }

    /** Calcula impuestos y total a partir del subtotal. Redondea a 2 decimales (centavos). */
    public static Factura calcular(DatosFactura datos, LocalDateTime fechaEmision) {
        BigDecimal subtotal = datos.getSubtotal().setScale(2, RoundingMode.HALF_UP);
        BigDecimal impuestos = subtotal.multiply(TASA_IVA).setScale(2, RoundingMode.HALF_UP);
        return new Factura(0, fechaEmision, datos, impuestos, subtotal.add(impuestos));
    }

    /** La misma factura con el número que le asignó MySQL al guardarla. */
    public Factura conNumero(int numero) {
        return new Factura(numero, fechaEmision, datos, impuestos, total);
    }

    public int getNumero() { return numero; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public DatosFactura getDatos() { return datos; }
    public int getOrdenId() { return datos.getServicioId(); }
    public BigDecimal getTotalRepuestos() { return datos.getTotalRepuestos(); }
    public BigDecimal getManoObra() { return datos.getManoObra(); }
    public BigDecimal getSubtotal() { return datos.getSubtotal(); }
    public BigDecimal getImpuestos() { return impuestos; }
    public BigDecimal getTotal() { return total; }

    @Override
    public String toString() {
        return "Factura " + numero + " | orden " + getOrdenId() + " | subtotal $" + getSubtotal()
                + " + IVA $" + impuestos + " = $" + total;
    }
}
