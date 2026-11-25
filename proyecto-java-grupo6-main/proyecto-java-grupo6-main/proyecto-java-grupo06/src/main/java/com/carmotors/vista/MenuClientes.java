package com.carmotors.vista;

import com.carmotors.controlador.ClienteControlador;
import com.carmotors.excepcion.CampoInvalidoException;
import com.carmotors.modelo.HistorialVehiculo;
import com.carmotors.servicio.HistorialClienteServicio.Recordatorio;
import java.util.List;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class MenuClientes {
    private final ClienteControlador clientes;

    public MenuClientes(ClienteControlador clientes) {
        this.clientes = clientes;
    }

    public void ejecutar(Scanner entrada) {
        try {
            while (true) {
                System.out.println("Clientes y vehículos");
                System.out.println("1. Registrar cliente");
                System.out.println("2. Registrar vehículo");
                System.out.println("3. Consultar historial");
                System.out.println("4. Consultar recordatorios pendientes");
                System.out.println("0. Volver al menú principal");
                String opcion = leer(entrada, "Opción");
                try {
                    switch (opcion) {
                        case "0":
                            return;
                        case "1":
                            registrarCliente(entrada);
                            break;
                        case "2":
                            registrarVehiculo(entrada);
                            break;
                        case "3":
                            historial(entrada);
                            break;
                        case "4":
                            recordatorios();
                            break;
                        default:
                            System.out.println("Opción inválida.");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                } catch (SQLException e) {
                    System.out.println("Error al consultar o guardar en MySQL. Código: " + e.getErrorCode());
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("Fin de la entrada.");
        }
    }

    private void registrarCliente(Scanner entrada) throws SQLException {
        String[] campos = {"Nombre", "Documento", "Teléfono", "Correo"};
        String[] datos = leerFormulario(entrada, campos);
        while (true) {
            try {
                clientes.registrarCliente(datos[0], datos[1], datos[2], datos[3]);
                System.out.println("Cliente registrado.");
                return;
            } catch (CampoInvalidoException e) {
                corregirCampo(entrada, campos, datos, e);
            }
        }
    }

    private void registrarVehiculo(Scanner entrada) throws SQLException {
        String[] campos = {"Documento", "Placa", "Marca", "Modelo"};
        String[] datos = leerFormulario(entrada, campos);
        while (true) {
            try {
                clientes.registrarVehiculo(datos[0], datos[1], datos[2], datos[3]);
                System.out.println("Vehículo registrado.");
                return;
            } catch (CampoInvalidoException e) {
                corregirCampo(entrada, campos, datos, e);
            }
        }
    }

    private String[] leerFormulario(Scanner entrada, String[] campos) {
        String[] datos = new String[campos.length];
        for (int i = 0; i < campos.length; i++) {

            datos[i] = EntradaConsola.texto(entrada, System.out, campos[i] + ": ", 255, false, false);
        }
        return datos;
    }

    private void corregirCampo(Scanner entrada, String[] campos, String[] datos,
                               CampoInvalidoException error) {
        System.out.println(error.getMessage());
        for (int i = 0; i < campos.length; i++) {
            if (campos[i].equals(error.getCampo())) {
                datos[i] = leer(entrada, campos[i]);
                return;
            }
        }
        throw error;
    }
    private void historial(Scanner entrada) throws SQLException {
        List<HistorialVehiculo> vehiculos = clientes.historial(EntradaConsola.texto(entrada, System.out, "Documento del cliente: ", 20, false, false));
        if (vehiculos.isEmpty()) System.out.println("No se encontraron vehículos para ese documento.");
        for (HistorialVehiculo vehiculo : vehiculos) {
            System.out.printf("%s | %s | %s %s%n", vehiculo.getNombre(), vehiculo.getPlaca(),
                    vehiculo.getMarca(), vehiculo.getModelo());
            if (vehiculo.getVisitas().isEmpty()) System.out.println("  Sin servicios registrados.");
            for (HistorialVehiculo.Visita visita : vehiculo.getVisitas()) {
                System.out.printf("  %s | %s | %s | %s%n", visita.getRecepcion(), visita.getTipo(),
                        visita.getEstado(), visita.getDescripcion());
            }
        }
    }

    private void recordatorios() throws SQLException {
        List<Recordatorio> pendientes = clientes.recordatorios();
        if (pendientes.isEmpty()) System.out.println("No hay recordatorios pendientes.");
        for (Recordatorio aviso : pendientes) {
            System.out.printf("%s | %s | %s | %s%n",
                    aviso.getCliente(), aviso.getEmail(), aviso.getPlaca(), aviso.getFecha());
        }
    }

    private static String leer(Scanner entrada, String campo) {
        return EntradaConsola.texto(entrada, System.out, campo + ": ", 255, false, false);
    }
}
