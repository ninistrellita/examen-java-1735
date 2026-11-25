package com.carmotors.vista;

import com.carmotors.controlador.InventarioControlador;
import com.carmotors.modelo.EvaluacionProveedor;
import com.carmotors.modelo.Lote;
import com.carmotors.modelo.Proveedor;
import com.carmotors.modelo.Repuesto;
import com.carmotors.modelo.ReservaRepuesto;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.NoSuchElementException;

public class MenuInventario {

    private final InventarioControlador controlador;

    public MenuInventario(InventarioControlador controlador) {
        this.controlador = controlador;
    }

    public void ejecutar(Scanner entrada) {
        while (true) {
            System.out.println();
            System.out.println("=== Inventario y Proveedores ===");
            System.out.println("1. Registrar proveedor");
            System.out.println("2. Listar proveedores");
            System.out.println("3. Registrar repuesto");
            System.out.println("4. Listar repuestos");
            System.out.println("5. Catálogo por proveedor");
            System.out.println("6. Registrar lote");
            System.out.println("7. Lotes por repuesto");
            System.out.println("8. Registrar evaluación de proveedor");
            System.out.println("9. Ver evaluaciones de un proveedor");
            System.out.println("10. Alertas (stock bajo / caducidad)");
            System.out.println("11. Reservar repuesto para una orden");
            System.out.println("12. Completar orden y descontar stock");
            System.out.println("13. Ver reservas de un servicio");
            System.out.println("0. Volver");
            System.out.print("Opción: ");

            if (!entrada.hasNextLine()) {
                return;
            }
            try {
                String opcion = EntradaConsola.texto(entrada, System.out, "", 2, false, false);
                switch (opcion) {
                    case "1":
                        registrarProveedor(entrada);
                        break;
                    case "2":
                        listarProveedores();
                        break;
                    case "3":
                        registrarRepuesto(entrada);
                        break;
                    case "4":
                        listarRepuestos();
                        break;
                    case "5":
                        catalogo(entrada);
                        break;
                    case "6":
                        registrarLote(entrada);
                        break;
                    case "7":
                        lotesPorRepuesto(entrada);
                        break;
                    case "8":
                        registrarEvaluacion(entrada);
                        break;
                    case "9":
                        verEvaluaciones(entrada);
                        break;
                    case "10":
                        mostrarAlertas();
                        break;
                    case "11":
                        reservar(entrada);
                        break;
                    case "12":
                        descontar(entrada);
                        break;
                    case "13":
                        verReservas(entrada);
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("Opción inválida.");
                }
            } catch (NoSuchElementException e) {
                return;
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Error de base de datos: " + e.getMessage());
            }
        }
    }

    private void registrarProveedor(Scanner entrada) throws SQLException {
        String nombre = leerNombre(entrada);
        String contacto = EntradaConsola.texto(entrada, System.out, "Contacto: ", 255, false, false);
        int id = controlador.registrarProveedor(nombre, contacto);
        System.out.println("Proveedor registrado con id=" + id);
    }

    private void listarProveedores() throws SQLException {
        List<Proveedor> lista = controlador.listarProveedores();
        if (lista.isEmpty()) {
            System.out.println("No hay proveedores.");
            return;
        }
        for (Proveedor p : lista) {
            System.out.println(p);
        }
    }

    private void registrarRepuesto(Scanner entrada) throws SQLException {
        String nombre = leerNombre(entrada);
        String marca = EntradaConsola.texto(entrada, System.out, "Marca: ", 100, false, false);
        System.out.print("Id tipo de repuesto: ");
        int tipoId = leerEntero(entrada);
        System.out.print("Id proveedor: ");
        int proveedorId = leerEntero(entrada);
        System.out.print("Precio compra: ");
        BigDecimal compra = leerDecimal(entrada);
        System.out.print("Precio venta: ");
        BigDecimal venta = leerDecimal(entrada);
        System.out.print("Stock inicial: ");
        int stock = EntradaConsola.entero(entrada, System.out, "", 0, Integer.MAX_VALUE);
        System.out.print("Stock mínimo: ");
        int minimo = EntradaConsola.entero(entrada, System.out, "", 0, Integer.MAX_VALUE);

        int id = controlador.registrarRepuesto(nombre, marca, tipoId, proveedorId, compra, venta, stock, minimo);
        System.out.println("Repuesto registrado con id=" + id);
    }

    private void listarRepuestos() throws SQLException {
        List<Repuesto> lista = controlador.listarRepuestos();
        if (lista.isEmpty()) {
            System.out.println("No hay repuestos.");
            return;
        }
        for (Repuesto r : lista) {
            System.out.println(r);
        }
    }

    private void catalogo(Scanner entrada) throws SQLException {
        System.out.print("Id proveedor: ");
        int proveedorId = leerEntero(entrada);
        List<Repuesto> lista = controlador.catalogo(proveedorId);
        if (lista.isEmpty()) {
            System.out.println("Ese proveedor no tiene repuestos en catálogo.");
            return;
        }
        System.out.println("Catálogo del proveedor " + proveedorId + ":");
        for (Repuesto r : lista) {
            System.out.println(r);
        }
    }

    private void registrarLote(Scanner entrada) throws SQLException {
        String codigo = EntradaConsola.texto(entrada, System.out, "Código de lote: ", 50, false, false);
        System.out.print("Id repuesto: ");
        int repuestoId = leerEntero(entrada);
        System.out.print("Id proveedor: ");
        int proveedorId = leerEntero(entrada);
        System.out.print("Fecha ingreso (AAAA-MM-DD): ");
        LocalDate ingreso = EntradaConsola.fecha(entrada, System.out, "Fecha: ", false);
        System.out.print("Fecha caducidad (AAAA-MM-DD o No aplica): ");
        LocalDate caducidad = leerFechaOpcional(entrada);
        LocalDate inicio = ingreso == null ? LocalDate.now() : ingreso;
        while (caducidad != null && caducidad.isBefore(inicio)) {
            System.out.println("La caducidad debe ser igual o posterior al ingreso.");
            caducidad = leerFechaOpcional(entrada);
        }
        System.out.print("Cantidad recibida: ");
        int cantidad = leerEntero(entrada);

        int id = controlador.registrarLote(codigo, repuestoId, proveedorId, ingreso, caducidad, cantidad);
        System.out.println("Lote registrado con id=" + id + " (stock del repuesto actualizado).");
    }

    private void lotesPorRepuesto(Scanner entrada) throws SQLException {
        System.out.print("Id repuesto: ");
        int repuestoId = leerEntero(entrada);

        List<Lote> lista = controlador.lotesPorRepuesto(repuestoId);
        if (lista.isEmpty()) {
            System.out.println("No hay lotes para ese repuesto.");
            return;
        }
        for (Lote l : lista) {
            System.out.println(l);
        }
    }

    private void registrarEvaluacion(Scanner entrada) throws SQLException {
        System.out.print("Id proveedor: ");
        int proveedorId = leerEntero(entrada);
        System.out.print("Calidad (1-5): ");
        int calidad = EntradaConsola.entero(entrada, System.out, "", 1, 5);
        System.out.print("Puntualidad (1-5): ");
        int puntualidad = EntradaConsola.entero(entrada, System.out, "", 1, 5);
        String obs = EntradaConsola.texto(entrada, System.out, "Observaciones (escribe Sin observaciones si corresponde): ", 500, false, false);
        int id = controlador.registrarEvaluacion(proveedorId, calidad, puntualidad, obs);
        System.out.println("Evaluación registrada con id=" + id);
    }

    private void verEvaluaciones(Scanner entrada) throws SQLException {
        System.out.print("Id proveedor: ");
        int proveedorId = leerEntero(entrada);
        List<EvaluacionProveedor> lista = controlador.evaluaciones(proveedorId);
        if (lista.isEmpty()) {
            System.out.println("Sin evaluaciones.");
            return;
        }
        for (EvaluacionProveedor e : lista) {
            System.out.println(e);
        }
    }

    private void mostrarAlertas() throws SQLException {
        List<String> lineas = controlador.alertas();
        for (String linea : lineas) {
            System.out.println(linea);
        }
    }

    private void reservar(Scanner entrada) throws SQLException {
        System.out.print("Id repuesto: ");
        int repuestoId = leerEntero(entrada);
        System.out.print("Cantidad: ");
        int cantidad = leerEntero(entrada);
        System.out.print("ID de la orden: ");
        int servicioId = leerEntero(entrada);
        ReservaRepuesto r = controlador.reservar(repuestoId, cantidad, servicioId);
        System.out.println("Reserva creada: " + r);
    }

    private void descontar(Scanner entrada) throws SQLException {
        System.out.print("Id servicio: ");
        int servicioId = leerEntero(entrada);
        controlador.descontarStock(servicioId);
        System.out.println("Stock descontado para el servicio " + servicioId);
    }

    private void verReservas(Scanner entrada) throws SQLException {
        System.out.print("Id servicio: ");
        int servicioId = leerEntero(entrada);
        List<ReservaRepuesto> lista = controlador.reservasDeServicio(servicioId);
        if (lista.isEmpty()) {
            System.out.println("No hay reservas para ese servicio.");
            return;
        }
        for (ReservaRepuesto r : lista) {
            System.out.println(r);
        }
    }

    private String leerNombre(Scanner entrada) {
        while (true) {
            String nombre = EntradaConsola.texto(entrada, System.out, "Nombre: ", 255, false, false);
            if (nombre.codePoints().anyMatch(Character::isLetter)) return nombre;
            System.out.println("El nombre debe contener letras. Puede incluir referencias como 12V o 3M.");
        }
    }
    private int leerEntero(Scanner entrada) {
        return EntradaConsola.entero(entrada, System.out, "Valor: ", 1, Integer.MAX_VALUE);
    }

    private BigDecimal leerDecimal(Scanner entrada) {
        return EntradaConsola.decimal(entrada, System.out, "Valor: ");
    }

    private LocalDate leerFechaOpcional(Scanner entrada) {
        while (true) {
            String texto = EntradaConsola.texto(entrada, System.out,
                    "Fecha o No aplica: ", 10, false, false);
            if (texto.equalsIgnoreCase("No aplica")) return null;
            try {
                if (texto.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) return LocalDate.parse(texto);
            } catch (DateTimeParseException e) {
                // Vuelve a pedir el mismo campo.
            }
            System.out.println("Ingresa una fecha válida o escribe No aplica.");
        }
    }
}