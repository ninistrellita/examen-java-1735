package com.carmotors;

import com.carmotors.config.ConexionFactory;
import com.carmotors.controlador.ReporteControlador;
import com.carmotors.dao.FacturaDao;
import com.carmotors.dao.FuenteReportes;
import com.carmotors.dao.ReporteDao;
import com.carmotors.modelo.AsignacionTecnico;
import com.carmotors.modelo.DatosFactura;
import com.carmotors.modelo.ProductividadTecnico;
import com.carmotors.modelo.RepuestoUsado;
import com.carmotors.modelo.ServicioReporte;
import com.carmotors.servicio.ReporteServicio;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Pruebas del día 3 de reportes.
 * Sin argumentos usa datos en memoria. Con --mysql también revisa la base de datos.
 */
public class ReportesDia3Prueba {
    @FunctionalInterface interface Accion { void ejecutar() throws Exception; }

    public static void main(String[] args) throws Exception {
        ReporteServicio reportes = new ReporteServicio(new DatosDePrueba());
        ReporteControlador controlador = new ReporteControlador(reportes, new FacturaDao());

        // ---------- 1. Casos límite ----------

        // Un periodo donde no hay órdenes: todo debe salir vacío, sin errores
        LocalDate eneroInicio = LocalDate.of(2025, 1, 1);
        LocalDate eneroFin = LocalDate.of(2025, 1, 31);
        comprobar(reportes.serviciosPorTipo(eneroInicio, eneroFin).isEmpty(), "Periodo vacío: por tipo");
        comprobar(reportes.productividadPorTecnico(eneroInicio, eneroFin).isEmpty(), "Periodo vacío: productividad");
        comprobar(reportes.consumoRepuestos(eneroInicio, eneroFin).isEmpty(), "Periodo vacío: consumo");

        // Un solo día (desde y hasta iguales): solo la orden 2, del 12 de octubre
        LocalDate dia = LocalDate.of(2026, 10, 12);
        comprobar(reportes.serviciosPorTipo(dia, dia).equals(Map.of("Correctivo", 1L)), "Un solo día");

        // La orden 7 es del 30 de noviembre a las 23:59 y no tiene tipo ni estado
        LocalDate ultimoDia = LocalDate.of(2026, 11, 30);
        comprobar(reportes.serviciosPorTipo(ultimoDia, ultimoDia).equals(Map.of("Sin tipo", 1L)),
                "Orden sin tipo (y las 23:59 del último día cuentan)");
        comprobar(reportes.serviciosPorEstado(ultimoDia, ultimoDia).equals(Map.of("Sin estado", 1L)),
                "Orden sin estado");
        comprobar(reportes.serviciosPorNombre(ultimoDia, ultimoDia).entrySet().iterator().next()
                .equals(Map.entry(ReporteServicio.SIN_NOMBRE, 1L)), "Orden sin nombre de servicio");
        comprobar(reportes.serviciosPorNombre(eneroInicio, eneroFin).values().stream().allMatch(n -> n == 0),
                "Periodo vacío: por nombre");

        // La orden 7 tampoco tiene técnicos: la productividad sigue igual que en el día 2
        List<ProductividadTecnico> productividad =
                reportes.productividadPorTecnico(LocalDate.of(2026, 10, 1), ultimoDia);
        comprobar(productividad.size() == 3, "Orden sin técnicos no cambia la productividad");
        comprobar(productividad.get(0).getTecnico().equals("Ana Torres"), "Ana es la primera");
        System.out.println("OK: casos límite.");

        // ---------- 2. Controlador ----------

        comprobar(controlador.serviciosPorTipo("2026-10-12", "2026-10-12").equals(Map.of("Correctivo", 1L)),
                "El controlador convierte las fechas");
        rechaza(() -> controlador.serviciosPorTipo("", "2026-11-30"));            // fecha vacía
        rechaza(() -> controlador.serviciosPorTipo("2026-13-01", "2026-11-30"));  // no existe el mes 13
        rechaza(() -> controlador.serviciosPorTipo("01/10/2026", "2026-11-30"));  // formato equivocado
        rechaza(() -> controlador.serviciosPorTipo("2026-11-30", "2026-10-01"));  // fechas al revés
        System.out.println("OK: controlador.");

        // ---------- 3. Datos de factura ----------

        // Orden con 2 repuestos: 1 batería de 380.000 y 4 aceites de 32.000 = 508.000
        List<RepuestoUsado> repuestos = List.of(
                new RepuestoUsado(3, 2, "Batería 12V", 1, new BigDecimal("380000")),
                new RepuestoUsado(3, 3, "Aceite 20W-50", 4, new BigDecimal("32000")));
        DatosFactura factura = new DatosFactura(3, "Completado", "sebas rosales", "1092942229",
                "DFM123", new BigDecimal("30000"), repuestos);
        comprobarDinero(factura.getTotalRepuestos(), "508000", "Total repuestos");
        comprobarDinero(factura.getSubtotal(), "538000", "Subtotal");
        comprobar(factura.sePuedeFacturar(), "Una orden Completado se puede facturar");

        // Orden sin repuestos: el subtotal es solo la mano de obra
        DatosFactura sinRepuestos = new DatosFactura(8, "Completado", "Carlos Gómez", "1090111222",
                "ABC123", new BigDecimal("45000"), List.of());
        comprobarDinero(sinRepuestos.getTotalRepuestos(), "0", "Sin repuestos: total repuestos");
        comprobarDinero(sinRepuestos.getSubtotal(), "45000", "Sin repuestos: subtotal");

        // Órdenes que no se pueden facturar
        comprobar(!new DatosFactura(2, "Facturado", "x", "x", "x", BigDecimal.ZERO, List.of()).sePuedeFacturar(),
                "Una orden ya facturada no se vuelve a facturar");
        comprobar(!new DatosFactura(5, "Pendiente", "x", "x", "x", BigDecimal.ZERO, List.of()).sePuedeFacturar(),
                "Una orden pendiente no se factura");
        System.out.println("OK: datos de factura " + factura);

        // ---------- 4. Con MySQL ----------
        if (args.length > 0 && args[0].equals("--mysql")) {
            probarConMySQL();
        }
    }

    /**
     * Usa una base de datos nueva con: schema.sql y data.sql.
     * Si se crean otras órdenes en octubre o noviembre de 2026, los números cambian.
     */
    private static void probarConMySQL() throws Exception {
        ReporteServicio real = new ReporteServicio(new ReporteDao());
        ReporteControlador controlador = new ReporteControlador(real, new FacturaDao());
        String desde = "2026-10-01";
        String hasta = "2026-11-30";

        comprobar(controlador.serviciosPorTipo(desde, hasta).equals(Map.of("Correctivo", 3L, "Preventivo", 2L)),
                "MySQL por tipo: " + controlador.serviciosPorTipo(desde, hasta));
        comprobar(controlador.serviciosPorNombre(desde, hasta).toString().equals("{Revisión de frenos=2, "
                + "Cambio de aceite=1, Alineación=1, Cambio de batería=1, Balanceo=0, "
                + "Revisión del sistema eléctrico=0}"), "MySQL por nombre: " + controlador.serviciosPorNombre(desde, hasta));
        comprobar(controlador.serviciosPorMes(desde, hasta).equals(
                Map.of(YearMonth.of(2026, 10), 3L, YearMonth.of(2026, 11), 2L)),
                "MySQL por mes: " + controlador.serviciosPorMes(desde, hasta));
        System.out.println("MySQL productividad: " + controlador.productividadPorTecnico(desde, hasta));
        System.out.println("MySQL consumo: " + controlador.consumoRepuestos(desde, hasta));

        // Busca el número de la orden "Reporte: batería descargada" (la que está Completado)
        int orden;
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             Statement st = conexion.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT MIN(id) FROM servicios WHERE descripcion_problema = 'Reporte: batería descargada'")) {
            rs.next();
            orden = rs.getInt(1);
        }
        DatosFactura factura = controlador.datosFactura(orden);
        comprobarDinero(factura.getTotalRepuestos(), "380000", "MySQL factura: repuestos");
        comprobarDinero(factura.getSubtotal(), "410000", "MySQL factura: subtotal");
        System.out.println("OK: MySQL. Factura " + factura);
    }

    /** Los datos del día 2 más la orden 7, que no tiene tipo, estado, técnicos ni repuestos. */
    private static class DatosDePrueba implements FuenteReportes {
        public List<ServicioReporte> servicios() {
            return List.of(
                    new ServicioReporte(1, "Preventivo", "Entregado", LocalDateTime.of(2026, 10, 5, 8, 0)),
                    new ServicioReporte(2, "Correctivo", "Facturado", LocalDateTime.of(2026, 10, 12, 9, 30)),
                    new ServicioReporte(3, "Correctivo", "Completado", LocalDateTime.of(2026, 10, 20, 14, 0)),
                    new ServicioReporte(4, "Preventivo", "En Proceso", LocalDateTime.of(2026, 11, 3, 10, 0)),
                    new ServicioReporte(5, "Correctivo", "Pendiente", LocalDateTime.of(2026, 11, 10, 16, 45)),
                    new ServicioReporte(7, null, null, LocalDateTime.of(2026, 11, 30, 23, 59)));
        }

        public List<AsignacionTecnico> asignaciones() {
            return List.of(
                    new AsignacionTecnico(1, 1, "Luis Pérez"),
                    new AsignacionTecnico(2, 1, "Luis Pérez"),
                    new AsignacionTecnico(2, 2, "Ana Torres"),
                    new AsignacionTecnico(3, 2, "Ana Torres"),
                    new AsignacionTecnico(4, 1, "Luis Pérez"),
                    new AsignacionTecnico(5, 3, "Mario Díaz"));
        }

        public List<RepuestoUsado> repuestosUsados() {
            return List.of(
                    new RepuestoUsado(2, 1, "Pastillas de freno", 2, new BigDecimal("95000")),
                    new RepuestoUsado(3, 2, "Batería 12V", 1, new BigDecimal("380000")),
                    new RepuestoUsado(4, 1, "Pastillas de freno", 1, new BigDecimal("95000")));
        }
    }

    // ---------- Ayudas ----------

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError("FALLÓ: " + mensaje);
    }

    private static void comprobarDinero(BigDecimal real, String esperado, String mensaje) {
        comprobar(real.compareTo(new BigDecimal(esperado)) == 0, mensaje + " (esperado " + esperado + ", salió " + real + ")");
    }

    /** Comprueba que la acción lanza IllegalArgumentException, es decir, que rechaza el dato malo. */
    private static void rechaza(Accion accion) throws Exception {
        try {
            accion.ejecutar();
        } catch (IllegalArgumentException esperado) {
            return;
        }
        throw new AssertionError("FALLÓ: se aceptó un dato inválido");
    }
}
