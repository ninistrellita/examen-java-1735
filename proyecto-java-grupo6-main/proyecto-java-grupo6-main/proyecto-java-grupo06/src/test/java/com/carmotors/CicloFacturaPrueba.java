package com.carmotors;

import com.carmotors.config.ConexionFactory;
import com.carmotors.dao.FacturaDao;
import com.carmotors.modelo.CicloOrden;
import com.carmotors.modelo.DatosFactura;
import com.carmotors.modelo.Factura;
import com.carmotors.modelo.ReservaRepuesto.EstadoReserva;
import com.carmotors.modelo.RepuestoUsado;
import com.carmotors.servicio.FacturaServicio;
import com.carmotors.servicio.FacturaServicio.FacturaEmitida;
import com.carmotors.servicio.FacturaTexto;
import com.carmotors.servicio.OrdenServicioServicio;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Pruebas del ciclo de la orden (estados, reservas y stock) y de la factura .txt.
 * Sin argumentos: solo cálculos y validaciones, sin base de datos.
 * Con --mysql: recorre el flujo completo en la base configurada con CARMOTORS_DB_URL.
 * Usa una base de pruebas: crea órdenes, un repuesto y facturas que no se borran.
 */
public class CicloFacturaPrueba {
    @FunctionalInterface interface Accion { void ejecutar() throws Exception; }

    public static void main(String[] args) throws Exception {
        // ---------- 1. Ciclo de estados ----------
        comprobar(CicloOrden.siguiente("Pendiente").equals(Optional.of("En Proceso")), "Pendiente -> En Proceso");
        comprobar(CicloOrden.siguiente("En Proceso").equals(Optional.of("Completado")), "En Proceso -> Completado");
        comprobar(CicloOrden.siguiente("Completado").equals(Optional.of("Facturado")), "Completado -> Facturado");
        comprobar(CicloOrden.siguiente("Facturado").equals(Optional.of("Entregado")), "Facturado -> Entregado");
        comprobar(CicloOrden.siguiente("Entregado").isEmpty(), "Entregado es el último");
        comprobar(CicloOrden.siguiente("Inventado").isEmpty(), "Un estado desconocido no avanza");
        comprobar(CicloOrden.admiteRepuestos("Pendiente") && CicloOrden.admiteRepuestos("En Proceso"),
                "Pendiente y En Proceso admiten repuestos");
        comprobar(!CicloOrden.admiteRepuestos("Completado") && !CicloOrden.admiteRepuestos("Facturado"),
                "Completado y Facturado no admiten repuestos");
        System.out.println("OK: ciclo de estados.");

        // ---------- 2. Cálculo de la factura ----------
        List<RepuestoUsado> repuestos = List.of(
                new RepuestoUsado(3, 2, "Batería 12V", 1, new BigDecimal("380000")),
                new RepuestoUsado(3, 3, "Aceite 20W-50", 4, new BigDecimal("32000")));
        DatosFactura datos = new DatosFactura(3, "Completado", "sebas rosales", "1092942229",
                "DFM123", new BigDecimal("30000"), repuestos);
        Factura factura = Factura.calcular(datos, LocalDateTime.of(2026, 11, 18, 15, 30));
        comprobarDinero(factura.getSubtotal(), "538000", "Subtotal");
        comprobarDinero(factura.getImpuestos(), "102220", "IVA 19%");
        comprobarDinero(factura.getTotal(), "640220", "Total");

        Factura sinRepuestos = Factura.calcular(new DatosFactura(8, "Completado", "Carlos Gómez", "1090111222",
                "ABC123", new BigDecimal("45000"), List.of()), LocalDateTime.now());
        comprobarDinero(sinRepuestos.getImpuestos(), "8550", "IVA sin repuestos");
        comprobarDinero(sinRepuestos.getTotal(), "53550", "Total sin repuestos");

        // 100.03 x 0,19 = 19,0057 -> se redondea a 19,01
        Factura conCentavos = Factura.calcular(new DatosFactura(9, "Completado", "x", "x", "x",
                new BigDecimal("100.03"), List.of()), LocalDateTime.now());
        comprobarDinero(conCentavos.getImpuestos(), "19.01", "Redondeo del IVA");
        comprobar(conCentavos.getImpuestos().scale() == 2, "El IVA queda con 2 decimales");
        System.out.println("OK: cálculo de la factura " + factura);

        // ---------- 3. Texto de la factura ----------
        String texto = FacturaTexto.generar(factura.conNumero(12));
        comprobar(texto.contains("FACTURA DE VENTA N.º 000012"), "Número de factura en el texto");
        comprobar(texto.contains("sebas rosales") && texto.contains("1092942229") && texto.contains("DFM123"),
                "Cliente, documento y placa en el texto");
        comprobar(texto.contains("Batería 12V") && texto.contains("Aceite 20W-50"), "Repuestos en el texto");
        comprobar(texto.contains("IVA (19%)") && texto.contains("$ 102.220,00"), "IVA en el texto");
        comprobar(texto.contains("TOTAL A PAGAR") && texto.contains("$ 640.220,00"), "Total en el texto");
        comprobar(FacturaTexto.generar(factura).contains("VISTA PREVIA"), "Sin número es vista previa");
        comprobar(FacturaTexto.generar(Factura.calcular(new DatosFactura(1, "Completado", null, null, null,
                BigDecimal.ZERO, List.of()), LocalDateTime.now())).contains("(sin repuestos)"),
                "Factura sin cliente ni repuestos no falla");
        System.out.println("OK: texto de la factura.");

        // ---------- 4. Validaciones sin abrir la base ----------
        OrdenServicioServicio sinBD = new OrdenServicioServicio(() -> { throw new AssertionError("No debe abrir BD"); });
        rechazar(() -> sinBD.agregarRepuesto(0, 1, 1));
        rechazar(() -> sinBD.agregarRepuesto(1, 0, 1));
        rechazar(() -> sinBD.agregarRepuesto(1, 1, 0));
        rechazar(() -> sinBD.agregarRepuesto(1, 1, -2));
        rechazar(() -> sinBD.avanzarEstado(0));
        rechazar(() -> sinBD.estadoDe(-1));
        rechazar(() -> sinBD.repuestosDe(0));
        FacturaServicio facturasSinBD = new FacturaServicio(() -> { throw new AssertionError("No debe abrir BD"); },
                new FacturaDao(), sinBD, Path.of("facturas"));
        rechazar(() -> facturasSinBD.emitir(0));
        rechazar(() -> facturasSinBD.vistaPrevia(-5));
        rechazar(() -> facturasSinBD.regenerarTexto(0));
        System.out.println("OK: 10 validaciones sin BD.");

        if (args.length > 0 && args[0].equals("--mysql")) {
            probarConMySQL();
        }
    }

    /**
     * Flujo completo: reservar, avanzar estados, descontar el stock una vez, facturar y entregar.
     * Base: schema.sql y data.sql.
     */
    private static void probarConMySQL() throws Exception {
        OrdenServicioServicio.Conexiones conexiones = ConexionFactory.getInstancia()::abrir;
        OrdenServicioServicio ordenes = new OrdenServicioServicio(conexiones);
        Path carpeta = Files.createTempDirectory("facturas-prueba");
        FacturaServicio facturacion = new FacturaServicio(conexiones, new FacturaDao(), ordenes, carpeta);

        int vehiculo = numero("SELECT id FROM vehiculos WHERE placa = 'TST001'");
        int tipo = numero("SELECT id FROM mantenimiento_tipos WHERE nombre = 'Preventivo'");
        int repuesto = crearRepuesto();          // stock 10, precio de venta 50.000

        int orden = ordenes.crear(vehiculo, tipo, "Prueba ciclo y factura", new BigDecimal("30000"));
        int otra = ordenes.crear(vehiculo, tipo, "Prueba ciclo: otra orden", new BigDecimal("10000"));
        comprobar(ordenes.estadoDe(orden).equals("Pendiente"), "La orden nace Pendiente");

        // Reservar no descuenta stock
        ordenes.agregarRepuesto(orden, repuesto, 3);
        comprobar(stock(repuesto) == 10, "Reservar no cambia el stock");

        // Lo reservado no está disponible para otra orden: 10 - 3 = 7
        rechazar(() -> ordenes.agregarRepuesto(otra, repuesto, 8));
        ordenes.agregarRepuesto(otra, repuesto, 7);
        rechazar(() -> ordenes.agregarRepuesto(orden, repuesto, 1));   // ya no queda nada disponible
        System.out.println("OK MySQL: reservas sin sobrevender.");

        // Facturar antes de tiempo no se puede
        rechazar(() -> facturacion.emitir(orden));
        rechazar(() -> facturacion.vistaPrevia(orden));

        // Avanzar hasta Completado: el stock baja UNA vez
        comprobar(ordenes.avanzarEstado(orden).equals("En Proceso"), "Pasa a En Proceso");
        comprobar(stock(repuesto) == 10, "En Proceso no descuenta");
        comprobar(ordenes.avanzarEstado(orden).equals("Completado"), "Pasa a Completado");
        comprobar(stock(repuesto) == 7, "Completado descuenta 3 (queda " + stock(repuesto) + ")");
        comprobar(ordenes.repuestosDe(orden).stream().allMatch(r -> r.getEstado() == EstadoReserva.DESCONTADO),
                "La reserva queda DESCONTADO");
        comprobar(numero("SELECT cantidad_usada FROM servicios_repuestos WHERE servicio_id = " + orden
                + " AND repuesto_id = " + repuesto) == 3, "Se anota el consumo en servicios_repuestos");

        // Avanzar otra vez desde Completado no se permite (Facturado solo desde facturación) y no descuenta
        rechazar(() -> ordenes.avanzarEstado(orden));
        rechazar(() -> ordenes.agregarRepuesto(orden, repuesto, 1));
        comprobar(stock(repuesto) == 7, "El stock no se descuenta dos veces");
        System.out.println("OK MySQL: ciclo hasta Completado y stock descontado una vez.");

        // Factura: (3 x 50.000 + 30.000) = 180.000; IVA 34.200; total 214.200
        Factura previa = facturacion.vistaPrevia(orden);
        comprobarDinero(previa.getTotal(), "214200", "Vista previa del total");
        FacturaEmitida emitida = facturacion.emitir(orden);
        comprobar(emitida.factura().getNumero() > 0, "La factura tiene número");
        comprobarDinero(emitida.factura().getImpuestos(), "34200", "IVA guardado");
        comprobarDinero(emitida.factura().getTotal(), "214200", "Total guardado");
        comprobar(Files.exists(emitida.archivo()), "Se escribió el .txt");
        String txt = Files.readString(emitida.archivo(), StandardCharsets.UTF_8);
        comprobar(txt.contains("$ 214.200,00") && txt.contains("TST001"), "Contenido del .txt");
        comprobar(ordenes.estadoDe(orden).equals("Facturado"), "La orden pasa a Facturado");
        comprobar(numero("SELECT COUNT(*) FROM facturas WHERE servicio_id = " + orden) == 1, "Una fila en facturas");
        rechazar(() -> facturacion.emitir(orden));                        // no se factura dos veces
        comprobar(numero("SELECT COUNT(*) FROM facturas WHERE servicio_id = " + orden) == 1, "Sigue una sola factura");
        System.out.println("OK MySQL: factura " + emitida.factura().getNumero() + " en " + emitida.archivo());

        // Volver a generar el .txt
        Files.delete(emitida.archivo());
        FacturaEmitida otraVez = facturacion.regenerarTexto(orden);
        comprobar(Files.exists(otraVez.archivo()), "Se regenera el .txt");
        comprobar(Files.readString(otraVez.archivo(), StandardCharsets.UTF_8).equals(txt), "El .txt regenerado es igual");
        rechazar(() -> facturacion.regenerarTexto(otra));                 // la otra orden no tiene factura

        // Entregar
        comprobar(ordenes.avanzarEstado(orden).equals("Entregado"), "Pasa a Entregado");
        comprobar(ordenes.consultar(orden).getFechaEntrega() != null, "Se guarda la fecha de entrega");
        rechazar(() -> ordenes.avanzarEstado(orden));                     // ya no avanza más
        System.out.println("OK MySQL: ciclo completo hasta Entregado.");

        // La otra orden: al completarla se descuentan sus 7 y el stock llega a 0
        ordenes.avanzarEstado(otra);
        ordenes.avanzarEstado(otra);
        comprobar(stock(repuesto) == 0, "La segunda orden descuenta sus 7");
        System.out.println("OK MySQL: dos órdenes, stock final 0.");
    }

    private static int crearRepuesto() throws SQLException {
        String sql = "INSERT INTO repuestos (nombre, repuesto_tipo_id, proveedor_id, precio_compra, precio_venta, "
                + "cantidad_stock, stock_minimo) VALUES (?, NULL, NULL, 30000, 50000, 10, 2)";
        try (Connection c = ConexionFactory.getInstancia().abrir();
             PreparedStatement p = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, "Filtro de prueba " + System.currentTimeMillis());
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                r.next();
                return r.getInt(1);
            }
        }
    }

    private static int stock(int repuesto) throws SQLException {
        return numero("SELECT cantidad_stock FROM repuestos WHERE id = " + repuesto);
    }

    private static int numero(String sql) throws SQLException {
        try (Connection c = ConexionFactory.getInstancia().abrir();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            if (!r.next()) throw new AssertionError("Sin resultado: " + sql);
            return r.getInt(1);
        }
    }

    private static void rechazar(Accion accion) throws Exception {
        try {
            accion.ejecutar();
        } catch (IllegalArgumentException esperado) {
            return;
        }
        throw new AssertionError("FALLÓ: se aceptó una operación inválida");
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError("FALLÓ: " + mensaje);
    }

    private static void comprobarDinero(BigDecimal real, String esperado, String mensaje) {
        comprobar(real.compareTo(new BigDecimal(esperado)) == 0,
                mensaje + " (esperado " + esperado + ", salió " + real + ")");
    }
}
