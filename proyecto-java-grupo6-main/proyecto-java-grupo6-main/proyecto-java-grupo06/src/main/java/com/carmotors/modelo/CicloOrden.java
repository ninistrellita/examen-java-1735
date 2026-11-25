package com.carmotors.modelo;

import java.util.List;
import java.util.Optional;

/**
 * Ciclo de vida de una orden de servicio.
 * Una orden solo avanza al estado siguiente: nunca salta ni retrocede.
 *
 * Pendiente -> En Proceso -> Completado -> Facturado -> Entregado
 */
public final class CicloOrden {
    public static final String PENDIENTE = "Pendiente";
    public static final String EN_PROCESO = "En Proceso";
    public static final String COMPLETADO = "Completado";
    public static final String FACTURADO = "Facturado";
    public static final String ENTREGADO = "Entregado";

    /** Los nombres coinciden con la tabla servicio_estados (database/data.sql). */
    private static final List<String> ORDEN = List.of(PENDIENTE, EN_PROCESO, COMPLETADO, FACTURADO, ENTREGADO);

    private CicloOrden() {
    }

    /** Estado al que puede pasar la orden, o vacío si ya terminó (Entregado) o el estado no existe. */
    public static Optional<String> siguiente(String actual) {
        int posicion = ORDEN.indexOf(actual);
        if (posicion < 0 || posicion == ORDEN.size() - 1) {
            return Optional.empty();
        }
        return Optional.of(ORDEN.get(posicion + 1));
    }

    /** Solo se reservan repuestos mientras la orden no se ha completado. */
    public static boolean admiteRepuestos(String estado) {
        return PENDIENTE.equals(estado) || EN_PROCESO.equals(estado);
    }
}
