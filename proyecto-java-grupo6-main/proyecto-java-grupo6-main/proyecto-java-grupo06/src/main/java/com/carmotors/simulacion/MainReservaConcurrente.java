package com.carmotors.simulacion;

import com.carmotors.config.ConexionFactory;
import com.carmotors.servicio.OrdenServicioServicio;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

// Nueva clase ejecutable independiente a la aplicacion con metodo Main para mostrar la solucion
public class  MainReservaConcurrente {
    public static class Resultado {
        public final int repuesto;
        public final int ordenGanadora;
        public final int exitos;
        public final int fallos;

        Resultado(int repuesto, int ordenGanadora, int exitos, int fallos) {
            this.repuesto = repuesto;
            this.ordenGanadora = ordenGanadora;
            this.exitos = exitos;
            this.fallos = fallos;
        }
    }
 //  Conexion con MySQL usando ConexionFactory.java
    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("--h2")) {
            String url = "jdbc:h2:mem:ejercicio_h;MODE=MySQL;DB_CLOSE_DELAY=-1";
            OrdenServicioServicio.Conexiones conexiones = () -> DriverManager.getConnection(url, "sa", "");
            try (Connection c = conexiones.abrir(); Statement s = c.createStatement()) {
                String esquema = Files.readString(Path.of("database/schema.sql"));
                esquema = esquema.substring(0, esquema.indexOf("SET @existe_nombre"));
                esquema = esquema.replaceAll("(?m)--.*$", "");
                for (String sentencia : esquema.split(";")) {
                    if (sentencia.stripLeading().startsWith("CREATE TABLE")) s.execute(sentencia);
                }
            }
            ejecutar(conexiones);
        } else if (args.length == 0) {
            ejecutar(ConexionFactory.getInstancia()::abrir);
        } else {
            throw new IllegalArgumentException("Ejecuta sin argumentos para MySQL.");
        }
    }
// Preparacion de los datos de prueba requeridos para el escenario
    public static Resultado ejecutar(OrdenServicioServicio.Conexiones conexiones) throws Exception {
        String identificador = UUID.randomUUID().toString().replace("-", "");
        int vehiculo;
        int repuesto;
        int mantenimiento;
        try (Connection c = conexiones.abrir()) {
            c.setAutoCommit(false);
            try {
                mantenimiento = catalogo(c, "mantenimiento_tipos", "Preventivo");
                catalogo(c, "servicio_estados", "Pendiente");
                catalogo(c, "servicio_estados", "En Proceso");
                catalogo(c, "servicio_estados", "Completado");
                int tipo = catalogo(c, "repuesto_tipos", "Mecánico");
                int cliente = insertar(c, "INSERT INTO clientes (nombre, documento_identidad, telefono, email) VALUES (?, ?, ?, ?)",
                        "Cliente Simulación", identificador.substring(0, 20), "3001234567",
                        "simulacion." + identificador + "@example.com");
                vehiculo = insertar(c, "INSERT INTO vehiculos (cliente_id, placa, marca, modelo) VALUES (?, ?, ?, ?)",
                        cliente, "H" + identificador.substring(0, 8), "Marca de prueba", "Modelo de prueba");
                int proveedor = insertar(c, "INSERT INTO proveedores (nombre, contacto) VALUES (?, ?)",
                        "Proveedor simulación " + identificador, "Contacto de prueba");
                repuesto = insertar(c, "INSERT INTO repuestos (nombre, repuesto_tipo_id, proveedor_id, precio_compra, "
                                + "precio_venta, cantidad_stock, stock_minimo) VALUES (?, ?, ?, ?, ?, ?, ?)",
                        "Filtro de Aceite XY-123", tipo, proveedor, new BigDecimal("12000"),
                        new BigDecimal("22000"), 1, 0);
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }

        // Instancias distintas comparten el monitor static del servicio.
        OrdenServicioServicio servicioA = new OrdenServicioServicio(conexiones);
        OrdenServicioServicio servicioB = new OrdenServicioServicio(conexiones);
        int ordenA = servicioA.crear(vehiculo, mantenimiento, "Cambio de aceite",
                "Simulación del técnico A", BigDecimal.ZERO);
        int ordenB = servicioB.crear(vehiculo, mantenimiento, "Cambio de aceite",
                "Simulación del técnico B", BigDecimal.ZERO);
        servicioA.asignarTecnico(ordenA, servicioA.registrarTecnico("Tecnico A"));
        servicioB.asignarTecnico(ordenB, servicioB.registrarTecnico("Tecnico B"));

        // Inicio del escenario simulado con los datos de prueba
        System.out.println("Iniciando simulación de reserva concurrente para 'Filtro de Aceite XY-123'...");
        System.out.println("ID del repuesto de prueba: " + repuesto + ". Stock inicial: " + stock(conexiones, repuesto));
        System.out.println("Órdenes distintas: " + ordenA + " y " + ordenB);
        CountDownLatch preparados = new CountDownLatch(2);
        CountDownLatch inicio = new CountDownLatch(1);
        AtomicInteger exitos = new AtomicInteger();
        AtomicInteger fallos = new AtomicInteger();
        AtomicInteger ganadora = new AtomicInteger();
        AtomicReference<Throwable> error = new AtomicReference<>();
        // Hilo de cada Tecnico
        Thread tecnicoA = hilo("Hilo-Técnico-A", servicioA, ordenA, repuesto, preparados, inicio, exitos, fallos, ganadora, error);
        Thread tecnicoB = hilo("Hilo-Técnico-B", servicioB, ordenB, repuesto, preparados, inicio, exitos, fallos, ganadora, error);
        tecnicoA.start();
        tecnicoB.start();
        if (!preparados.await(10, TimeUnit.SECONDS)) {
            tecnicoA.interrupt();
            tecnicoB.interrupt();
            throw new IllegalStateException("Los técnicos no llegaron al punto de inicio.");
        }
        inicio.countDown();
        tecnicoA.join(15000);
        tecnicoB.join(15000);
        if (tecnicoA.isAlive() || tecnicoB.isAlive()) {
            tecnicoA.interrupt();
            tecnicoB.interrupt();
            throw new IllegalStateException("La simulación no terminó a tiempo.");
        }
        if (error.get() != null) throw new IllegalStateException("Falló la simulación.", error.get());
        int finalStock = stock(conexiones, repuesto);
        System.out.println("Simulación finalizada. Stock final del repuesto: " + finalStock);
        System.out.println("Reservas exitosas: " + exitos.get() + ". Fallos por falta de stock: " + fallos.get());
        if (finalStock != 0 || exitos.get() != 1 || fallos.get() != 1) {
            throw new AssertionError("Debe ganar un solo técnico y quedar stock 0.");
        }
        return new Resultado(repuesto, ganadora.get(), exitos.get(), fallos.get());
    }

    private static Thread hilo(String nombre, OrdenServicioServicio servicio, int orden, int repuesto,
                               CountDownLatch preparados, CountDownLatch inicio, AtomicInteger exitos,
                               AtomicInteger fallos, AtomicInteger ganadora, AtomicReference<Throwable> error) {
        return new Thread(() -> {
            try {
                System.out.println(Thread.currentThread().getName() + ": Intentando reservar el repuesto...");
                preparados.countDown();
                inicio.await();
                servicio.agregarRepuesto(orden, repuesto, 1,
                        mensaje -> System.out.println(Thread.currentThread().getName() + ": " + mensaje));
                exitos.incrementAndGet();
                ganadora.set(orden);
            } catch (IllegalArgumentException e) {
                if ("No hay suficiente stock.".equals(e.getMessage())) {
                    fallos.incrementAndGet();
                    System.out.println(Thread.currentThread().getName() + ": Fallo en la reserva. " + e.getMessage());
                } else {
                    error.compareAndSet(null, e);
                }
            } catch (Throwable e) {
                error.compareAndSet(null, e);
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            }
        }, nombre);
    }

    public static int stock(OrdenServicioServicio.Conexiones conexiones, int repuesto) throws SQLException {
        try (Connection c = conexiones.abrir();
             PreparedStatement p = c.prepareStatement("SELECT cantidad_stock FROM repuestos WHERE id = ?")) {
            p.setInt(1, repuesto);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("Repuesto inexistente.");
                return r.getInt(1);
            }
        }
    }

    private static int catalogo(Connection c, String tabla, String nombre) throws SQLException {
        // Los nombres de tabla provienen únicamente de las tres llamadas fijas de esta clase.
        try (PreparedStatement p = c.prepareStatement("SELECT id FROM " + tabla + " WHERE nombre = ?")) {
            p.setString(1, nombre);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) return r.getInt(1);
            }
        }
        return insertar(c, "INSERT INTO " + tabla + " (nombre) VALUES (?)", nombre);
    }

    private static int insertar(Connection c, String sql, Object... valores) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < valores.length; i++) p.setObject(i + 1, valores[i]);
            if (p.executeUpdate() != 1) throw new SQLException("No se insertó el dato de simulación.");
            try (ResultSet r = p.getGeneratedKeys()) {
                if (!r.next()) throw new SQLException("No se obtuvo el ID.");
                return r.getInt(1);
            }
        }
    }
}