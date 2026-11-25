package com.carmotors;

import com.carmotors.controlador.ClienteControlador;
import com.carmotors.dao.ClienteDao;
import com.carmotors.dao.VehiculoDao;
import com.carmotors.modelo.Cliente;
import com.carmotors.modelo.Vehiculo;
import com.carmotors.servicio.*;
import com.carmotors.vista.MenuClientes;
import java.util.Scanner;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class RegistroCorreccionPrueba {
    static class Clientes extends ClienteDao {
        int cantidad;
        Cliente guardado;
        public void insertar(Cliente cliente) { cantidad++; guardado = cliente; }
    }
    static class Vehiculos extends VehiculoDao {
        int cantidad;
        public boolean insertar(Vehiculo vehiculo) { cantidad++; return true; }
    }
    public static void main(String[] args) throws Exception {
        Clientes dao = new Clientes();
        Vehiculos vehiculos = new Vehiculos();
        ClienteControlador controlador = new ClienteControlador(new RegistroClienteServicio(dao),
                new RegistroVehiculoServicio(vehiculos), null);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            new MenuClientes(controlador).ejecutar(new Scanner(
                "1\nAna\n123\n3001234567\n\nmal\n  ANA@example.com  \n"
                + "2\n123\n\nabc123\nRenault\nLogan\n0\n"
            .replace("\n", "\n")));
        } finally { System.setOut(original); }
        String salida = buffer.toString(StandardCharsets.UTF_8);
        if (dao.cantidad != 1 || !"ana@example.com".equals(dao.guardado.getEmail())
                || !"3001234567".equals(dao.guardado.getTelefono()) || vehiculos.cantidad != 1) {
            throw new AssertionError("El formulario no conservó datos o duplicó escrituras.");
        }
        if (!salida.contains("Ingresa entre 1 y 255 caracteres.") || !salida.contains("Ingresa un correo válido.")
                || !salida.contains("Vehículo registrado.")) {
            throw new AssertionError("No se volvieron a pedir los campos inválidos.");
        }
        try {
            controlador.registrarCliente("Ana", "123", "", "");
            throw new AssertionError("El servicio aceptó correo vacío.");
        } catch (com.carmotors.excepcion.CampoInvalidoException esperado) {}
        if (dao.cantidad != 1) throw new AssertionError("Se guardó un cliente inválido.");
        new MenuClientes(controlador).ejecutar(new Scanner("1\n"));
        System.out.println("OK: correo vacío e inválido, normalización, conservación de campos, placa vacía y fin de entrada.");
    }
}