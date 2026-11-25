package com.carmotors;

import com.carmotors.servicio.OrdenServicioServicio;
import java.math.BigDecimal;
import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.carmotors.controlador.OrdenServicioControlador;
import com.carmotors.vista.MenuOrdenes;

public class OrdenServicioPrueba {
    @FunctionalInterface interface Accion { void ejecutar() throws Exception; }

    public static void main(String[] args) throws Exception {
        OrdenServicioServicio sinBD = new OrdenServicioServicio(() -> { throw new AssertionError("No debe abrir BD"); });
        rechazar(() -> sinBD.crear(0, 1, "Problema", BigDecimal.ZERO));
        rechazar(() -> sinBD.crear(1, 0, "Problema", BigDecimal.ZERO));
        rechazar(() -> sinBD.crear(1, 1, " ", BigDecimal.ZERO));
        rechazar(() -> sinBD.crear(1, 1, "Problema", null));
        rechazar(() -> sinBD.crear(1, 1, "Problema", new BigDecimal("-1")));
        rechazar(() -> sinBD.crear(1, 1, "Problema", new BigDecimal("1.001")));
        rechazar(() -> sinBD.crear(1, 1, "Problema", new BigDecimal("100000000")));
        rechazar(() -> sinBD.crear(1, 1, "", "Problema", BigDecimal.ZERO));
        rechazar(() -> sinBD.crear(1, 1, "   ", "Problema", BigDecimal.ZERO));
        rechazar(() -> sinBD.crear(1, 1, "a".repeat(101), "Problema", BigDecimal.ZERO));
        rechazar(() -> sinBD.consultar(0));
        rechazar(() -> sinBD.tecnicosDe(0));
        rechazar(() -> sinBD.asignarTecnico(0, 1));
        rechazar(() -> sinBD.asignarTecnico(1, 0));
        System.out.println("OK: 14 validaciones sin BD.");
        if (args.length == 0) return;
        boolean h2 = args[0].equals("--h2");
        if (!h2 && !args[0].equals("--mysql")) throw new IllegalArgumentException("Usa --h2 o --mysql");
        String url = h2 ? "jdbc:h2:mem:ordenes;MODE=MySQL;DB_CLOSE_DELAY=-1" : System.getenv("CARMOTORS_DB_URL");
        String usuario = h2 ? "sa" : System.getenv("CARMOTORS_DB_USER");
        String clave = h2 ? "" : System.getenv("CARMOTORS_DB_PASSWORD");
        if (h2) {
            try (Connection c = DriverManager.getConnection(url, usuario, clave);
                 Statement s = c.createStatement()) {
                String esquema = Files.readString(Path.of("database", "schema.sql"));
                esquema = esquema.substring(0, esquema.indexOf("SET @existe_nombre"));
                for (String sentencia : esquema.split(";")) {
                    String ddl = sentencia.stripLeading();
                    if (ddl.startsWith("CREATE TABLE")) s.execute(ddl);
                }
                s.executeUpdate("INSERT INTO servicio_estados (id, nombre) VALUES "
                        + "(1, 'Pendiente'), (2, 'En Proceso'), (3, 'Completado')");
                s.executeUpdate("INSERT INTO mantenimiento_tipos (id, nombre) VALUES (1, 'Preventivo')");
                s.executeUpdate("INSERT INTO clientes (id, nombre, documento_identidad, email) "
                        + "VALUES (1, 'Cliente Prueba', '10000', 'cliente@example.com')");
                s.executeUpdate("INSERT INTO vehiculos (id, placa, cliente_id) VALUES (1, 'TST001', 1)");
                s.executeUpdate("INSERT INTO tecnicos (id, nombre) VALUES (1, 'Tecnico de prueba')");
            }
        }
        var servicio = new OrdenServicioServicio(() -> DriverManager.getConnection(url, usuario, clave));
        int id = servicio.crear(1, 1, "Cambio de aceite", "Mantenimiento del motor", new BigDecimal("50000.00"));
        try {
            var orden = servicio.consultar(id);
            comprobar(orden.getVehiculoId() == 1 && orden.getEstadoId() == 1, "Datos de creación incorrectos");
            comprobar(orden.getDescripcionProblema().equals("Mantenimiento del motor"), "Descripción incorrecta");
            comprobar("Cambio de aceite".equals(orden.getNombreServicio()), "Nombre de servicio incorrecto");
            comprobar(orden.getCostoManoObra().compareTo(new BigDecimal("50000.00")) == 0, "Costo incorrecto");
            comprobar(servicio.listar().stream().anyMatch(o -> o.getId() == id), "Falta orden en listado");
            servicio.asignarTecnico(id, 1);
            servicio.asignarTecnico(id, 1);
            comprobar(servicio.tecnicosDe(id).size() == 1, "Técnico duplicado");
            rechazar(() -> servicio.consultar(Integer.MAX_VALUE));
            try {
                servicio.asignarTecnico(id, Integer.MAX_VALUE);
                throw new AssertionError("Se asignó un técnico inexistente");
            } catch (SQLIntegrityConstraintViolationException esperado) { /* FK y rollback */ }
            comprobar(servicio.tecnicosDe(id).size() == 1, "La asignación fallida alteró los técnicos");
            var buffer = new ByteArrayOutputStream();
            String acciones = "x\n2\nno-numero\n" + id + "\n3\n4\n" + id + "\n1\n0\n";
            try (var entrada = new Scanner(acciones);
                 var salida = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
                new MenuOrdenes(new OrdenServicioControlador(servicio), entrada, salida).mostrar();
            }
            String consola = buffer.toString(StandardCharsets.UTF_8);
            comprobar(consola.contains("Selecciona una opción") && consola.contains("Ingresa un entero entre"),
                    "La consola no maneja entradas inválidas");
            comprobar(consola.contains("Cambio de aceite") && consola.contains("Técnicos: [1]")
                    && consola.contains("Técnico asignado."), "Falló el flujo MVC de consulta y asignación");
            comprobar(servicio.tecnicosDe(id).size() == 1, "La consola duplicó la asignación");
            if (h2) verificarCierreConcurrente(servicio, id);
            System.out.println("OK: creación, consulta, asignación, rollback y consola con " + (h2 ? "H2 (modo MySQL)." : "MySQL."));
        } finally {
            try (Connection c = DriverManager.getConnection(url, usuario, clave)) {
                try (PreparedStatement p = c.prepareStatement("DELETE FROM servicios_tecnicos WHERE servicio_id = ?")) {
                    p.setInt(1, id);
                    p.executeUpdate();
                }
                try (PreparedStatement p = c.prepareStatement("DELETE FROM servicios WHERE id = ?")) {
                    p.setInt(1, id);
                    p.executeUpdate();
                }
            }
        }
    }

    private static void verificarCierreConcurrente(OrdenServicioServicio servicio, int id) throws Exception {
        servicio.avanzarEstado(id);
        var avisos = new AtomicInteger();
        servicio.suscribir(aviso -> {
            if (aviso.orden() == id && "cliente@example.com".equals(aviso.correo())
                    && "Completado".equals(aviso.estado())) avisos.incrementAndGet();
        });
        var preparados = new CountDownLatch(2);
        var inicio = new CountDownLatch(1);
        var ejecutor = Executors.newFixedThreadPool(2);
        try {
            java.util.concurrent.Callable<Boolean> completar = () -> {
                preparados.countDown();
                inicio.await();
                try {
                    return "Completado".equals(servicio.avanzarEstado(id));
                } catch (IllegalArgumentException yaCompletada) {
                    return false;
                }
            };
            var primero = ejecutor.submit(completar);
            var segundo = ejecutor.submit(completar);
            comprobar(preparados.await(5, TimeUnit.SECONDS), "Los dos hilos no iniciaron");
            inicio.countDown();
            comprobar(primero.get(5, TimeUnit.SECONDS) ^ segundo.get(5, TimeUnit.SECONDS),
                    "Solo un hilo debe completar la orden");
            comprobar(avisos.get() == 1, "Debe emitirse un solo aviso de cierre para el cliente");
            comprobar("Completado".equals(servicio.estadoDe(id)), "La orden debe quedar completada");
            System.out.println("OK: dos hilos, un cierre y un aviso.");
        } finally {
            ejecutor.shutdownNow();
        }
    }

    private static void rechazar(Accion accion) throws Exception {
        try { accion.ejecutar(); } catch (IllegalArgumentException esperado) { return; }
        throw new AssertionError("Se aceptó una entrada inválida");
    }
    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }
}
