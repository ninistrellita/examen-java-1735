package com.carmotors;

import com.carmotors.controlador.OrdenServicioControlador;
import com.carmotors.controlador.ClienteControlador;
import com.carmotors.dao.ClienteDao;
import com.carmotors.dao.VehiculoDao;
import com.carmotors.modelo.Cliente;
import com.carmotors.servicio.OrdenServicioServicio;
import com.carmotors.servicio.RegistroClienteServicio;
import com.carmotors.servicio.RegistroVehiculoServicio;
import com.carmotors.vista.EntradaConsola;
import com.carmotors.vista.MenuClientes;
import com.carmotors.vista.MenuOrdenes;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Scanner;

public class ValidacionesFormularioPrueba {
    private static class Ordenes extends OrdenServicioServicio {
        int creadas;

        Ordenes() {
            super(() -> { throw new AssertionError("No debe abrir MySQL."); });
        }

        @Override
        public int crear(int vehiculo, int tipo, String nombre, String problema, BigDecimal costo) {
            comprobar(vehiculo == 1 && tipo == 2, "IDs incorrectos");
            comprobar(nombre.equals("Cambio de aceite"), "Nombre incorrecto");
            comprobar(problema.equals("Ruido al encender motor 2"), "Descripción incorrecta");
            comprobar(costo.compareTo(new BigDecimal("50000")) == 0, "Costo incorrecto");
            return ++creadas;
        }
    }

    private static class Clientes extends ClienteDao {
        Cliente guardado;
        int cantidad;

        @Override
        public void insertar(Cliente cliente) {
            guardado = cliente;
            cantidad++;
        }
    }

    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        comprobar(EntradaConsola.entero(new Scanner("\nabc\n-1\n0\n2147483648\n3\n"),
                salida, "ID: ", 1, Integer.MAX_VALUE) == 3, "Reintento del ID");
        comprobar(EntradaConsola.decimal(new Scanner("\nabc\n-5\n1.234\n100000000\n25,50\n"),
                salida, "Precio: ").equals(new BigDecimal("25.50")), "Reintento del precio");
        comprobar(EntradaConsola.fecha(new Scanner("\n2026-02-30\n2026-11-19\n"),
                salida, "Fecha: ", false).equals(LocalDate.of(2026, 11, 19)), "Reintento de fecha");
        comprobar(EntradaConsola.texto(new Scanner("\n123\nAna María\n"), salida, "Nombre: ",
                255, false, true).equals("Ana María"), "Reintento del nombre");

        Ordenes ordenes = new Ordenes();
        String flujo = "1\n\nabc\n0\n1\n\nx\n2\n123\nCambio de aceite\n"
                + "\nRuido al encender motor 2\n\nletras\n-1\n1.111\n50000\n0\n";
        new MenuOrdenes(new OrdenServicioControlador(ordenes), new Scanner(flujo), salida).mostrar();
        comprobar(ordenes.creadas == 1, "El formulario abandonó o duplicó la orden");

        Clientes clientes = new Clientes();
        ClienteControlador controlador = new ClienteControlador(new RegistroClienteServicio(clientes),
                new RegistroVehiculoServicio(new VehiculoDao()), null);
        PrintStream original = System.out;
        try {
            System.setOut(salida);
            new MenuClientes(controlador).ejecutar(new Scanner(
                    "1\n123\nabc\ntelefono\nana@example.com\nAna María\n00123\n3001234567\n0\n"));
        } finally {
            System.setOut(original);
        }
        comprobar(clientes.cantidad == 1, "Cliente guardado más de una vez o formulario perdido");
        comprobar(clientes.guardado.getNombre().equals("Ana María"), "Nombre no corregido");
        comprobar(clientes.guardado.getDocumento().equals("00123"), "Documento perdió ceros");
        comprobar(clientes.guardado.getTelefono().equals("3001234567"), "Teléfono no corregido");

        OrdenServicioServicio servicio = new OrdenServicioServicio(() -> {
            throw new AssertionError("No debe consultar MySQL con datos inválidos.");
        });
        try {
            servicio.crear(1, 1, "123", "Problema", BigDecimal.ZERO);
            throw new AssertionError("El servicio aceptó un nombre numérico");
        } catch (IllegalArgumentException esperado) { }
        try {
            servicio.registrarTecnico("123");
            throw new AssertionError("El servicio aceptó un técnico numérico");
        } catch (IllegalArgumentException esperado) { }
        new MenuOrdenes(new OrdenServicioControlador(ordenes), new Scanner("1\n"), salida).mostrar();
        System.out.println("OK: reintentos por campo, límites, nombres, documentos, teléfono, orden única y fin de entrada.");
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }
}