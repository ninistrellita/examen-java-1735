package com.carmotors;

import com.carmotors.config.ConexionFactory;
import com.carmotors.dao.FacturaDao;
import com.carmotors.servicio.OrdenServicioServicio;
import com.carmotors.servicio.FacturaServicio;
import java.sql.*;
import java.nio.file.*;
import java.math.BigDecimal;
import java.util.UUID;

public class IntegracionSistemaPrueba {
    static String base = "carmotors_merge_" + UUID.randomUUID().toString().replace("-", "");
    static Connection abrir() throws SQLException {
        Connection c = ConexionFactory.getInstancia().abrir();
        c.setCatalog(base);
        return c;
    }
    static void ejecutar(Connection c, Path archivo) throws Exception {
        String sql = Files.readString(archivo).replace("carmotors", base).replaceAll("(?m)--.*$", "");
        for (String sentencia : sql.split(";")) {
            if (!sentencia.isBlank()) {
                try (Statement s = c.createStatement()) { s.execute(sentencia); }
            }
        }
    }
    static int numero(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            r.next();
            return r.getInt(1);
        }
    }
    static void comprobar(boolean valor, String mensaje) {
        if (!valor) throw new AssertionError(mensaje);
    }
    public static void main(String[] args) throws Exception {
        boolean creada = false;
        try (Connection admin = ConexionFactory.getInstancia().abrir()) {
            try {
                try (Statement s = admin.createStatement()) { s.execute("CREATE DATABASE " + base); }
                creada = true;
                admin.setCatalog(base);
                ejecutar(admin, Path.of("proyecto-java-grupo06/database/schema.sql"));
                ejecutar(admin, Path.of("proyecto-java-grupo06/database/data.sql"));
                String[] tablas = {"clientes", "vehiculos", "proveedores", "repuestos", "repuesto_tipos",
                        "mantenimiento_tipos", "servicio_estados", "servicios", "tecnicos",
                        "servicios_tecnicos", "servicios_repuestos", "lotes", "evaluaciones_proveedor",
                        "reservas_repuesto", "facturas"};
                for (String tabla : tablas) {
                    comprobar(numero(admin, "SELECT COUNT(*) FROM " + tabla) > 0,
                            "Faltan datos de presentación en " + tabla);
                }
                int antes = numero(admin, "SELECT COUNT(*) FROM servicios");
                ejecutar(admin, Path.of("proyecto-java-grupo06/database/schema.sql"));
                ejecutar(admin, Path.of("proyecto-java-grupo06/database/data.sql"));
                comprobar(numero(admin, "SELECT COUNT(*) FROM servicios") == antes, "Carga duplicó órdenes");
                OrdenServicioServicio ordenes = new OrdenServicioServicio(IntegracionSistemaPrueba::abrir);
                int vehiculo = numero(admin, "SELECT id FROM vehiculos WHERE placa='TST001'");
                int tipo = numero(admin, "SELECT id FROM mantenimiento_tipos WHERE nombre='Preventivo'");
                int repuesto = numero(admin, "SELECT MIN(id) FROM repuestos");
                int stock = numero(admin, "SELECT cantidad_stock FROM repuestos WHERE id=" + repuesto);
                int orden = ordenes.crear(vehiculo, tipo, "Cambio de aceite", "Prueba de integración", new BigDecimal("30000"));
                ordenes.asignarRepuesto(orden, repuesto, 2);
                comprobar(numero(admin, "SELECT cantidad_stock FROM repuestos WHERE id=" + repuesto) == stock, "Reservar descontó stock");
                BigDecimal precio = ordenes.repuestosDe(orden).get(0).getPrecioUnitarioVenta();
                comprobar(precio != null, "Falta precio de la asignación");
                try (Statement s = admin.createStatement()) {
                    s.executeUpdate("UPDATE repuestos SET precio_venta=precio_venta+100 WHERE id=" + repuesto);
                }
                int completado = numero(admin, "SELECT id FROM servicio_estados WHERE nombre='Completado'");
                try {
                    ordenes.cambiarEstado(orden, completado);
                    throw new AssertionError("Permitió saltar estado");
                } catch (IllegalArgumentException esperado) {}
                comprobar(ordenes.avanzarEstado(orden).equals("En Proceso"), "No avanzó");
                ordenes.cambiarEstado(orden, completado);
                comprobar(numero(admin, "SELECT cantidad_stock FROM repuestos WHERE id=" + repuesto) == stock - 2, "Stock incorrecto");
                try (Statement s = admin.createStatement(); ResultSet rs = s.executeQuery(
                        "SELECT precio_unitario_venta FROM servicios_repuestos WHERE servicio_id=" + orden)) {
                    rs.next();
                    comprobar(rs.getBigDecimal(1).compareTo(precio) == 0, "Cambió precio reservado");
                }
                try { ordenes.avanzarEstado(orden); throw new AssertionError("Facturó sin emitir"); }
                catch (IllegalArgumentException esperado) {}
                comprobar(numero(admin, "SELECT cantidad_stock FROM repuestos WHERE id=" + repuesto) == stock - 2, "Doble descuento");
                Path carpeta = Path.of("target/facturas-integracion");
                FacturaServicio facturas = new FacturaServicio(IntegracionSistemaPrueba::abrir, new FacturaDao(), ordenes, carpeta);
                facturas.vistaPrevia(orden);
                var emitida = facturas.emitir(orden);
                comprobar(Files.readString(emitida.archivo()).contains("TOTAL A PAGAR"), "Falta factura txt");
                try { facturas.emitir(orden); throw new AssertionError("Duplicó factura"); }
                catch (IllegalArgumentException esperado) {}
                facturas.regenerarTexto(orden);
                comprobar(ordenes.avanzarEstado(orden).equals("Entregado"), "No entregó");
                comprobar(ordenes.consultar(orden).getFechaEntrega() != null, "Falta fecha entrega");
                System.out.println("OK MySQL: scripts repetibles, reserva, precios, transiciones, descuento único, factura txt, regeneración y entrega.");
            } finally {
                if (creada) {
                    try (Statement s = admin.createStatement()) { s.execute("DROP DATABASE " + base); }
                }
            }
        }
    }
}