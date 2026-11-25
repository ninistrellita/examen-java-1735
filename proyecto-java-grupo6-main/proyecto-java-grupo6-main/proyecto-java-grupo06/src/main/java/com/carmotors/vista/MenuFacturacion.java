package com.carmotors.vista;

import com.carmotors.controlador.FacturaControlador;
import com.carmotors.modelo.Factura;
import com.carmotors.servicio.FacturaServicio.FacturaEmitida;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Consola de facturación: vista previa, emitir la factura .txt y volver a generarla. */
public class MenuFacturacion {
    private final FacturaControlador facturas;

    public MenuFacturacion(FacturaControlador facturas) {
        this.facturas = facturas;
    }

    public void ejecutar(Scanner entrada) {
        while (true) {
            System.out.println("Facturación");
            System.out.println("1. Vista previa de la factura de una orden");
            System.out.println("2. Emitir factura (.txt)");
            System.out.println("3. Volver a generar el .txt de una orden facturada");
            System.out.println("0. Volver");
            try {
                String opcion = leer(entrada, "Opción");
                switch (opcion) {
                    case "0":
                        return;
                    case "1":
                        System.out.print(facturas.texto(facturas.vistaPrevia(orden(entrada))));
                        break;
                    case "2":
                        emitir(entrada);
                        break;
                    case "3":
                        mostrar(facturas.regenerarTexto(orden(entrada)));
                        break;
                    default:
                        System.out.println("Opción inválida.");
                }
            } catch (NoSuchElementException e) {
                return;
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un número de orden válido.");
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(e.getMessage());
            } catch (SQLException e) {
                System.out.println("Error de MySQL. Código: " + e.getErrorCode() + " | " + e.getMessage());
            }
        }
    }

    private void emitir(Scanner entrada) throws SQLException {
        int orden = orden(entrada);
        Factura previa = facturas.vistaPrevia(orden);
        System.out.print(facturas.texto(previa));
        String respuesta = leer(entrada, "¿Emitir esta factura? La orden pasará a Facturado (s/n)");
        while (!respuesta.equalsIgnoreCase("s") && !respuesta.equalsIgnoreCase("n")) {
            System.out.println("Responde s o n.");
            respuesta = leer(entrada, "¿Emitir esta factura? (s/n)");
        }
        if (!respuesta.equalsIgnoreCase("s")) {
            System.out.println("No se emitió la factura.");
            return;
        }
        mostrar(facturas.emitir(orden));
    }

    private void mostrar(FacturaEmitida emitida) {
        System.out.println("Factura N.º " + emitida.factura().getNumero() + " | total $"
                + emitida.factura().getTotal());
        System.out.println("Archivo: " + emitida.archivo());
    }

    private int orden(Scanner entrada) {
        return EntradaConsola.entero(entrada, System.out, "ID de la orden: ", 1, Integer.MAX_VALUE);
    }

    private String leer(Scanner entrada, String campo) {
        return EntradaConsola.texto(entrada, System.out, campo + ": ", 255, false, false);
    }
}
