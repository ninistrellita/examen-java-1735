package com.carmotors.servicio;

import com.carmotors.modelo.DatosFactura;
import com.carmotors.modelo.Factura;
import com.carmotors.modelo.RepuestoUsado;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;

/** Arma el texto de la factura que se guarda en el .txt (y que se muestra como vista previa). */
public final class FacturaTexto {
    private static final int ANCHO = 64;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private FacturaTexto() {
    }

    public static String generar(Factura factura) {
        DatosFactura datos = factura.getDatos();
        StringBuilder t = new StringBuilder();
        String doble = "=".repeat(ANCHO);
        String simple = "-".repeat(ANCHO);

        t.append(doble).append('\n');
        t.append(centrar("CARMOTORS - TALLER AUTOMOTRIZ")).append('\n');
        t.append(centrar(factura.getNumero() == 0
                ? "VISTA PREVIA (todavía sin número)"
                : String.format("FACTURA DE VENTA N.º %06d", factura.getNumero()))).append('\n');
        t.append(doble).append('\n');
        t.append(campo("Fecha de emisión", factura.getFechaEmision().format(FECHA)));
        t.append(campo("Orden de servicio", String.valueOf(datos.getServicioId())));
        t.append(campo("Cliente", datos.getCliente()));
        t.append(campo("Documento", datos.getDocumento()));
        t.append(campo("Vehículo (placa)", datos.getPlaca()));
        t.append(simple).append('\n');

        t.append(String.format("%-26s %5s %15s %15s%n", "Repuesto", "Cant.", "Precio unit.", "Subtotal"));
        if (datos.getRepuestos().isEmpty()) {
            t.append("(sin repuestos)").append('\n');
        }
        for (RepuestoUsado r : datos.getRepuestos()) {
            t.append(String.format("%-26s %5d %15s %15s%n", recortar(r.getRepuesto(), 26), r.getCantidad(),
                    dinero(r.getPrecioUnitario()), dinero(r.getSubtotal())));
        }
        t.append(simple).append('\n');

        t.append(total("Total repuestos", factura.getTotalRepuestos()));
        t.append(total("Mano de obra", factura.getManoObra()));
        t.append(total("Subtotal", factura.getSubtotal()));
        String tasa = Factura.TASA_IVA.movePointRight(2).stripTrailingZeros().toPlainString();
        t.append(total("IVA (" + tasa + "%)", factura.getImpuestos()));
        t.append(doble).append('\n');
        t.append(total("TOTAL A PAGAR", factura.getTotal()));
        t.append(doble).append('\n');
        return t.toString();
    }

    /** Formato colombiano: punto para miles y coma para decimales. Ej.: 1.234.567,89 */
    static String dinero(BigDecimal valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        DecimalFormat formato = new DecimalFormat("#,##0.00", simbolos);
        return "$ " + formato.format(valor.setScale(2, RoundingMode.HALF_UP));
    }

    private static String campo(String nombre, String valor) {
        return String.format("%-18s: %s%n", nombre, valor == null || valor.isBlank() ? "-" : valor);
    }

    private static String total(String nombre, BigDecimal valor) {
        return String.format("%-40s %23s%n", nombre, dinero(valor));
    }

    private static String centrar(String texto) {
        int margen = Math.max(0, (ANCHO - texto.length()) / 2);
        return " ".repeat(margen) + texto;
    }

    private static String recortar(String texto, int maximo) {
        if (texto == null) {
            return "-";
        }
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1) + "…";
    }
}
