package com.carmotors.vista;

import com.carmotors.controlador.ReporteControlador;
import com.carmotors.modelo.DatosFactura;
import com.carmotors.modelo.RepuestoUsado;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class MenuReportes {
    private final ReporteControlador reportes;

    public MenuReportes(ReporteControlador reportes) {
        this.reportes = reportes;
    }

    public void ejecutar(Scanner entrada) {
        while (true) {
            System.out.println("Reportes gerenciales");
            System.out.println("1. Servicios más solicitados");
            System.out.println("2. Productividad por técnico");
            System.out.println("3. Consumo de repuestos");
            System.out.println("4. Servicios completados y exportación TXT");
            System.out.println("0. Volver");
            try {
                String opcion = leer(entrada, "Opción");
                if (opcion.equals("0")) {
                    return;
                }
                if (!opcion.equals("1") && !opcion.equals("2") && !opcion.equals("3") && !opcion.equals("4")) {
                    System.out.println("Opción inválida.");
                    continue;
                }
                java.time.LocalDate inicio = EntradaConsola.fecha(entrada, System.out, "Desde (AAAA-MM-DD): ", false);
                java.time.LocalDate fin = EntradaConsola.fecha(entrada, System.out, "Hasta (AAAA-MM-DD): ", false);
                while (fin.isBefore(inicio)) {
                    System.out.println("La fecha final debe ser igual o posterior a la inicial.");
                    fin = EntradaConsola.fecha(entrada, System.out, "Hasta (AAAA-MM-DD): ", false);
                }
                String desde = inicio.toString();
                String hasta = fin.toString();
                switch (opcion) {
                    case "1":
                        mostrarServicios(reportes.serviciosPorNombre(desde, hasta));
                        break;
                    case "2":
                        mostrar(reportes.productividadPorTecnico(desde, hasta));
                        break;
                    case "3":
                        mostrar(reportes.consumoRepuestos(desde, hasta));
                        break;
                    case "4":
                        mostrarCompletados(entrada, desde, hasta);
                        break;
                }
            } catch (NoSuchElementException e) {
                return;
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println(e.getMessage());
            } catch (SQLException e) {
                System.out.println("Error de MySQL. Código: " + e.getErrorCode());
            } catch (IOException e) {
                System.out.println("No se pudo guardar el reporte: " + e.getMessage());
            }
        }
    }

    private void mostrarCompletados(Scanner entrada, String desde, String hasta) throws SQLException, IOException {
        String reporte = reportes.completados(desde, hasta);
        System.out.print(reporte);
        System.out.print("Ruta del archivo .txt (Enter para volver): ");
        String ruta = entrada.nextLine().strip();
        if (!ruta.isEmpty()) {
            Path destino = Path.of(ruta);
            reportes.exportarCompletados(reporte, destino);
            System.out.println("Reporte guardado en " + destino.toAbsolutePath());
        }
    }

    public void consultarFactura(Scanner entrada) {
        try {
            int id = EntradaConsola.entero(entrada, System.out, "ID de la orden (0 para volver): ", 0, Integer.MAX_VALUE);
            if (id == 0) {
                return;
            }
            DatosFactura factura = reportes.datosFactura(id);
            System.out.println(factura);
            System.out.println("Documento: " + factura.getDocumento());
            for (RepuestoUsado repuesto : factura.getRepuestos()) {
                System.out.printf("%s | %d × %s = %s%n", repuesto.getRepuesto(), repuesto.getCantidad(),
                        repuesto.getPrecioUnitario(), repuesto.getSubtotal());
            }
        } catch (NoSuchElementException e) {
            return;
        } catch (NumberFormatException e) {
            System.out.println("Ingresa un ID válido.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println(e.getMessage());
        } catch (SQLException e) {
            System.out.println("Error de MySQL. Código: " + e.getErrorCode());
        }
    }

    private void mostrarServicios(Map<String, Long> conteo) {
        long total = conteo.values().stream().mapToLong(Long::longValue).sum();
        if (total == 0) {
            System.out.println("No hay resultados para ese período.");
            return;
        }
        conteo.forEach((servicio, cantidad) -> System.out.printf("%-32s %d%n", servicio, cantidad));
        System.out.println("Total de órdenes: " + total);
    }

    private void mostrar(Iterable<?> resultados) {
        if (!resultados.iterator().hasNext()) {
            System.out.println("No hay resultados para ese período.");
        }
        for (Object resultado : resultados) {
            System.out.println(resultado);
        }
    }

    private String leer(Scanner entrada, String campo) {
        return EntradaConsola.texto(entrada, System.out, campo + ": ", 255, false, false);
    }
}
