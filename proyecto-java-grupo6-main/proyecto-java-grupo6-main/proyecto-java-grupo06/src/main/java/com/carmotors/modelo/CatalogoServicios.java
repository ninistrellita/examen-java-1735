package com.carmotors.modelo;

import java.util.List;

/**
 * Lista de servicios que se pueden pedir en una orden. Es la única copia de la lista:
 * la usan el menú de órdenes (para elegir), OrdenServicioServicio (para validar)
 * y los reportes (para contar). Si se agrega un servicio, se agrega solo aquí.
 */
public final class CatalogoServicios {
    public static final List<String> NOMBRES = List.of("Cambio de aceite", "Alineación", "Balanceo",
            "Revisión de frenos", "Cambio de batería", "Revisión del sistema eléctrico");

    private CatalogoServicios() {
    }

    public static boolean contiene(String nombre) {
        return nombre != null && NOMBRES.contains(nombre);
    }
}
