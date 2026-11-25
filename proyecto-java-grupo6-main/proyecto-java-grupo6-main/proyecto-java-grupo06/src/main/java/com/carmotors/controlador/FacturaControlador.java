package com.carmotors.controlador;

import com.carmotors.modelo.Factura;
import com.carmotors.servicio.FacturaServicio;
import com.carmotors.servicio.FacturaServicio.FacturaEmitida;
import com.carmotors.servicio.FacturaTexto;
import java.sql.SQLException;
import java.util.Objects;

/** El menú de facturación le pasa el número de orden y el controlador llama al servicio. */
public class FacturaControlador {
    private final FacturaServicio servicio;

    public FacturaControlador(FacturaServicio servicio) {
        this.servicio = Objects.requireNonNull(servicio);
    }

    public Factura vistaPrevia(int orden) throws SQLException {
        return servicio.vistaPrevia(orden);
    }

    public FacturaEmitida emitir(int orden) throws SQLException {
        return servicio.emitir(orden);
    }

    public FacturaEmitida regenerarTexto(int orden) throws SQLException {
        return servicio.regenerarTexto(orden);
    }

    public String texto(Factura factura) {
        return FacturaTexto.generar(factura);
    }
}
