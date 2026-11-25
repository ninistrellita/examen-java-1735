package com.carmotors;

import com.carmotors.controlador.*;
import com.carmotors.dao.*;
import com.carmotors.modelo.Cliente;
import com.carmotors.servicio.*;
import com.carmotors.vista.*;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class CamposObligatoriosPrueba {
    private static class Clientes extends ClienteDao {
        int cantidad;
        @Override public void insertar(Cliente cliente) {
            comprobar(cliente.getNombre().equals("Ana"), "Nombre vacío aceptado");
            comprobar(cliente.getDocumento().equals("00123"), "Documento incorrecto");
            comprobar(cliente.getTelefono().equals("3001234567"), "Teléfono vacío aceptado");
            comprobar(cliente.getEmail().equals("ana@example.com"), "Correo vacío aceptado");
            cantidad++;
        }
    }

    private static class Inventario extends InventarioControlador {
        int proveedores;
        int repuestos;
        int evaluaciones;
        int lotes;
        Inventario() { super(null); }

        @Override public int registrarProveedor(String nombre, String contacto) {
            comprobar(nombre.equals("Proveedor Uno") && contacto.equals("3001234567"), "Proveedor incompleto");
            return ++proveedores;
        }

        @Override public int registrarRepuesto(String nombre, String marca, int tipo, int proveedor,
                                               BigDecimal compra, BigDecimal venta, int stock, int minimo) {
            comprobar(nombre.equals("Filtro") && marca.equals("Marca Uno"), "Repuesto incompleto");
            comprobar(tipo == 1 && proveedor == 1 && stock == 0 && minimo == 0, "Números incorrectos");
            return ++repuestos;
        }

        @Override public int registrarLote(String codigo, int repuesto, int proveedor,
                java.time.LocalDate ingreso, java.time.LocalDate caducidad, int cantidad) {
            comprobar(codigo.equals("LOTE-TEST") && ingreso.equals(java.time.LocalDate.of(2026, 11, 19))
                    && caducidad == null && cantidad == 2, "Lote incompleto o fecha incorrecta");
            return ++lotes;
        }

        @Override public int registrarEvaluacion(int proveedor, int calidad, int puntualidad, String obs) {
            comprobar(obs.equals("Sin observaciones") && calidad == 5 && puntualidad == 5,
                    "Evaluación incompleta");
            return ++evaluaciones;
        }
    }

    public static void main(String[] args) throws Exception {
        PrintStream original = System.out;
        InputStream originalEntrada = System.in;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        try {
            System.setOut(salida);
            Clientes dao = new Clientes();
            ClienteControlador clientes = new ClienteControlador(new RegistroClienteServicio(dao),
                    new RegistroVehiculoServicio(new VehiculoDao()), null);
            new MenuClientes(clientes).ejecutar(new Scanner(
                    "\n1\n\n   \nAna\n\n00123\n\n3001234567\n\nana@example.com\n0\n"));
            comprobar(dao.cantidad == 1, "No se guardó exactamente un cliente");

            Inventario inventario = new Inventario();
            String entradas = "\n1\n\nProveedor Uno\n\n3001234567\n"
                    + "3\n\nFiltro\n\nMarca Uno\n\n1\n\n1\n\n100\n\n200\n\n0\n\n0\n"
                    + "8\n\n1\n\n5\n\n5\n\nSin observaciones\n6\n\nLOTE-TEST\n\n1\n\n1\n\n2026-11-19\n\nNo aplica\n\n2\n0\n";
            new MenuInventario(inventario).ejecutar(new Scanner(entradas));
            comprobar(inventario.proveedores == 1 && inventario.repuestos == 1 && inventario.evaluaciones == 1 && inventario.lotes == 1,
                    "Se abandonó un formulario de inventario");

            new MenuFacturacion(null).ejecutar(new Scanner("\n0\n"));
            new MenuReportes(null).ejecutar(new Scanner("\n0\n"));
            new MenuOrdenes(null, new Scanner("\n0\n"), salida).mostrar();
            System.setIn(new ByteArrayInputStream("\n0\n".getBytes(StandardCharsets.UTF_8)));
            new MenuPrincipal(null, null, null, null, null).ejecutar();
            new MenuInventario(inventario).ejecutar(new Scanner("\n"));

            try {
                new RegistroClienteServicio(dao).registrarCliente("Ana", "123", "", "ana@example.com");
                throw new AssertionError("Servicio aceptó teléfono vacío");
            } catch (IllegalArgumentException esperado) { }
            comprobar(dao.cantidad == 1, "Se guardó cliente inválido");
        } finally {
            System.setOut(original);
            System.setIn(originalEntrada);
        }
        comprobar(buffer.toString(StandardCharsets.UTF_8).contains("Ingresa entre 1 y"),
                "No se avisó del campo vacío");
        System.out.println("OK: campos obligatorios y reintentos en clientes, inventario y todos los menús.");
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }
}