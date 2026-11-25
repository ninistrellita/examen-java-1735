package com.carmotors;

import com.carmotors.dao.FuenteReportes;
import com.carmotors.dao.ReporteDao;
import com.carmotors.modelo.AsignacionTecnico;
import com.carmotors.modelo.ConsumoRepuesto;
import com.carmotors.modelo.ProductividadTecnico;
import com.carmotors.modelo.RepuestoUsado;
import com.carmotors.modelo.ServicioReporte;
import com.carmotors.servicio.ReporteServicio;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/** Pruebas del día 2 de reportes. Sin argumentos usa datos en memoria; con --mysql lee la BD. */
public class ReportesDia2Prueba {
    @FunctionalInterface interface Accion { void ejecutar() throws Exception; }

    private static final LocalDate DESDE = LocalDate.of(2026, 10, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 11, 30);

    public static void main(String[] args) throws Exception {
        ReporteServicio sinBD = new ReporteServicio(new FuenteReportes() {
            public List<ServicioReporte> servicios() { throw new AssertionError("No debe leer datos"); }
            public List<AsignacionTecnico> asignaciones() { throw new AssertionError("No debe leer datos"); }
            public List<RepuestoUsado> repuestosUsados() { throw new AssertionError("No debe leer datos"); }
        });
        rechazar(() -> sinBD.serviciosPorTipo(null, HASTA));
        rechazar(() -> sinBD.serviciosPorNombre(HASTA, DESDE));
        rechazar(() -> sinBD.productividadPorTecnico(DESDE, null));
        rechazar(() -> sinBD.consumoRepuestos(HASTA, DESDE));
        System.out.println("OK: 4 validaciones de periodo.");

        ReporteServicio reportes = new ReporteServicio(new DatosEnMemoria());

        // 1. Servicios solicitados (la orden 6 es de septiembre y queda fuera del periodo)
        comprobar(reportes.serviciosPorTipo(DESDE, HASTA).equals(Map.of("Correctivo", 3L, "Preventivo", 2L)),
                "Servicios por tipo");
        comprobar(reportes.serviciosPorEstado(DESDE, HASTA).equals(Map.of("Entregado", 1L, "Facturado", 1L,
                "Completado", 1L, "En Proceso", 1L, "Pendiente", 1L)), "Servicios por estado");
        comprobar(reportes.serviciosPorMes(DESDE, HASTA).equals(Map.of(YearMonth.of(2026, 10), 3L,
                YearMonth.of(2026, 11), 2L)), "Servicios por mes");
        comprobar(reportes.serviciosPorTipo(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30))
                .equals(Map.of("Preventivo", 1L)), "Filtro de periodo");
        System.out.println("OK: servicios solicitados.");

        // 1b. Servicios por nombre: de más a menos; con la misma cantidad, en el orden de la lista;
        // los de la lista sin órdenes salen con 0
        comprobar(reportes.serviciosPorNombre(DESDE, HASTA).toString().equals("{Revisión de frenos=2, "
                + "Cambio de aceite=1, Alineación=1, Cambio de batería=1, Balanceo=0, "
                + "Revisión del sistema eléctrico=0}"), "Servicios por nombre: " + reportes.serviciosPorNombre(DESDE, HASTA));
        comprobar(reportes.serviciosPorNombre(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30))
                .entrySet().iterator().next().equals(Map.entry("Balanceo", 1L)), "Por nombre: filtro de periodo");

        // Órdenes sin nombre y con un nombre que no está en la lista: van a grupos aparte, al final
        ReporteServicio especiales = new ReporteServicio(new FuenteReportes() {
            public List<ServicioReporte> servicios() {
                LocalDateTime dia = LocalDateTime.of(2026, 10, 1, 8, 0);
                return List.of(
                        new ServicioReporte(1, "Preventivo", "Cambio de aceite", "Pendiente", dia),
                        new ServicioReporte(2, "Preventivo", null, "Pendiente", dia),
                        new ServicioReporte(3, "Preventivo", " ", "Pendiente", dia),
                        new ServicioReporte(4, "Correctivo", "Pintura", "Pendiente", dia));
            }
            public List<AsignacionTecnico> asignaciones() { return List.of(); }
            public List<RepuestoUsado> repuestosUsados() { return List.of(); }
        });
        Map<String, Long> porNombre = especiales.serviciosPorNombre(DESDE, HASTA);
        comprobar(porNombre.get(ReporteServicio.SIN_NOMBRE) == 2L && porNombre.get(ReporteServicio.OTRO) == 1L
                && porNombre.get("Cambio de aceite") == 1L && porNombre.size() == 8, "Grupos especiales: " + porNombre);
        comprobar(List.copyOf(porNombre.keySet()).subList(0, 2).equals(List.of(ReporteServicio.SIN_NOMBRE,
                "Cambio de aceite")), "Orden de grupos especiales: " + porNombre);
        System.out.println("OK: servicios por nombre " + reportes.serviciosPorNombre(DESDE, HASTA));

        // 2. Productividad: Ana 2/2, Luis 2/3, Mario 0/1 (Luis no suma la orden 6, fuera del periodo)
        List<ProductividadTecnico> productividad = reportes.productividadPorTecnico(DESDE, HASTA);
        comprobar(productividad.size() == 3, "Cantidad de técnicos");
        comprobarTecnico(productividad.get(0), "Ana Torres", 2, 2, 100);
        comprobarTecnico(productividad.get(1), "Luis Pérez", 3, 2, 67);
        comprobarTecnico(productividad.get(2), "Mario Díaz", 1, 0, 0);
        System.out.println("OK: productividad por técnico " + productividad);

        // 3. Consumo: pastillas 2+1 = 3 u. ($285.000) en 2 órdenes; batería 1 u. ($380.000)
        List<ConsumoRepuesto> consumo = reportes.consumoRepuestos(DESDE, HASTA);
        comprobar(consumo.size() == 2, "Cantidad de repuestos");
        comprobarConsumo(consumo.get(0), "Pastillas de freno", 3, "285000.00", 2);
        comprobarConsumo(consumo.get(1), "Batería 12V", 1, "380000.00", 1);
        System.out.println("OK: consumo de repuestos " + consumo);

        if (args.length == 0 || !args[0].equals("--mysql")) return;
        ReporteServicio real = new ReporteServicio(new ReporteDao());
        LocalDate desde = LocalDate.of(2000, 1, 1), hasta = LocalDate.now().plusYears(1);
        System.out.println("MySQL - por nombre: " + real.serviciosPorNombre(desde, hasta));
        System.out.println("MySQL - por tipo: " + real.serviciosPorTipo(desde, hasta));
        System.out.println("MySQL - por estado: " + real.serviciosPorEstado(desde, hasta));
        System.out.println("MySQL - por mes: " + real.serviciosPorMes(desde, hasta));
        System.out.println("MySQL - productividad: " + real.productividadPorTecnico(desde, hasta));
        System.out.println("MySQL - consumo: " + real.consumoRepuestos(desde, hasta));
    }

    /** Los mismos datos que database/data.sql, más una orden fuera del periodo. */
    private static class DatosEnMemoria implements FuenteReportes {
        public List<ServicioReporte> servicios() {
            return List.of(
                    new ServicioReporte(1, "Preventivo", "Cambio de aceite", "Entregado", LocalDateTime.of(2026, 10, 5, 8, 0)),
                    new ServicioReporte(2, "Correctivo", "Revisión de frenos", "Facturado", LocalDateTime.of(2026, 10, 12, 9, 30)),
                    new ServicioReporte(3, "Correctivo", "Cambio de batería", "Completado", LocalDateTime.of(2026, 10, 20, 14, 0)),
                    new ServicioReporte(4, "Preventivo", "Revisión de frenos", "En Proceso", LocalDateTime.of(2026, 11, 3, 10, 0)),
                    new ServicioReporte(5, "Correctivo", "Alineación", "Pendiente", LocalDateTime.of(2026, 11, 10, 16, 45)),
                    new ServicioReporte(6, "Preventivo", "Balanceo", "Entregado", LocalDateTime.of(2026, 9, 15, 8, 0)));
        }

        public List<AsignacionTecnico> asignaciones() {
            return List.of(
                    new AsignacionTecnico(1, 1, "Luis Pérez"),
                    new AsignacionTecnico(2, 1, "Luis Pérez"),
                    new AsignacionTecnico(2, 2, "Ana Torres"),
                    new AsignacionTecnico(3, 2, "Ana Torres"),
                    new AsignacionTecnico(4, 1, "Luis Pérez"),
                    new AsignacionTecnico(5, 3, "Mario Díaz"),
                    new AsignacionTecnico(6, 1, "Luis Pérez"));
        }

        public List<RepuestoUsado> repuestosUsados() {
            return List.of(
                    new RepuestoUsado(2, 1, "Pastillas de freno", 2, new BigDecimal("95000.00")),
                    new RepuestoUsado(3, 2, "Batería 12V", 1, new BigDecimal("380000.00")),
                    new RepuestoUsado(4, 1, "Pastillas de freno", 1, new BigDecimal("95000.00")),
                    new RepuestoUsado(6, 2, "Batería 12V", 5, new BigDecimal("380000.00")));
        }
    }

    private static void comprobarTecnico(ProductividadTecnico p, String nombre, long asignadas,
                                         long completadas, int porcentaje) {
        comprobar(p.getTecnico().equals(nombre) && p.getAsignadas() == asignadas
                && p.getCompletadas() == completadas && p.getPorcentajeCompletadas() == porcentaje,
                "Productividad incorrecta: " + p);
    }

    private static void comprobarConsumo(ConsumoRepuesto c, String nombre, long unidades, String valor, long ordenes) {
        comprobar(c.getRepuesto().equals(nombre) && c.getUnidades() == unidades
                && c.getValorTotal().compareTo(new BigDecimal(valor)) == 0 && c.getOrdenes() == ordenes,
                "Consumo incorrecto: " + c);
    }

    private static void rechazar(Accion accion) throws Exception {
        try { accion.ejecutar(); } catch (IllegalArgumentException esperado) { return; }
        throw new AssertionError("Se aceptó un periodo inválido");
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }
}
