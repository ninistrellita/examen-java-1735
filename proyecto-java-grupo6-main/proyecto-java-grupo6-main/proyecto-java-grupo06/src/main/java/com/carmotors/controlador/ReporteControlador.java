package com.carmotors.controlador;

import com.carmotors.dao.FacturaDao;
import com.carmotors.dao.ReporteDao;
import com.carmotors.modelo.ConsumoRepuesto;
import com.carmotors.modelo.DatosFactura;
import com.carmotors.modelo.ProductividadTecnico;
import com.carmotors.servicio.ReporteServicio;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * La vista (el menú) le pasa lo que escribió el usuario y el controlador llama al servicio.
 */
public class ReporteControlador {
    private final ReporteServicio reportes;
    private final FacturaDao facturas;
    private final ReporteDao reporteDao;

    public ReporteControlador(ReporteServicio reportes, FacturaDao facturas) {
        this(reportes, facturas, new ReporteDao());
    }

    public ReporteControlador(ReporteServicio reportes, FacturaDao facturas, ReporteDao reporteDao) {
        this.reportes = reportes;
        this.facturas = facturas;
        this.reporteDao = reporteDao;
    }

    public String completados(String desde, String hasta) throws SQLException {
        LocalDate inicio = convertirFecha(desde);
        LocalDate fin = convertirFecha(hasta);
        if (inicio.isAfter(fin)) throw new IllegalArgumentException("La fecha inicial es posterior a la final.");
        var filas = reporteDao.completados(inicio, fin);
        String salto = System.lineSeparator();
        StringBuilder texto = new StringBuilder("Servicios completados").append(salto);
        texto.append("Periodo de recepción: ").append(inicio).append(" a ").append(fin).append(salto);
        texto.append("Total: ").append(filas.size()).append(salto);
        filas.forEach(fila -> texto.append(fila).append(salto));
        return texto.toString();
    }

    public void exportarCompletados(String contenido, Path destino) throws IOException {
        if (destino.getFileName() == null || !destino.getFileName().toString().endsWith(".txt")) {
            throw new IllegalArgumentException("Escribe un nombre de archivo terminado en .txt.");
        }
        Files.writeString(destino, contenido, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE);
    }

    public Map<String, Long> serviciosPorNombre(String desde, String hasta) throws SQLException {
        return reportes.serviciosPorNombre(convertirFecha(desde), convertirFecha(hasta));
    }

    public Map<String, Long> serviciosPorTipo(String desde, String hasta) throws SQLException {
        return reportes.serviciosPorTipo(convertirFecha(desde), convertirFecha(hasta));
    }

    public Map<String, Long> serviciosPorEstado(String desde, String hasta) throws SQLException {
        return reportes.serviciosPorEstado(convertirFecha(desde), convertirFecha(hasta));
    }

    public Map<YearMonth, Long> serviciosPorMes(String desde, String hasta) throws SQLException {
        return reportes.serviciosPorMes(convertirFecha(desde), convertirFecha(hasta));
    }

    public List<ProductividadTecnico> productividadPorTecnico(String desde, String hasta) throws SQLException {
        return reportes.productividadPorTecnico(convertirFecha(desde), convertirFecha(hasta));
    }

    public List<ConsumoRepuesto> consumoRepuestos(String desde, String hasta) throws SQLException {
        return reportes.consumoRepuestos(convertirFecha(desde), convertirFecha(hasta));
    }

    /** Datos para la factura de una orden. Solo si la orden existe y está "Completado". */
    public DatosFactura datosFactura(int servicioId) throws SQLException {
        DatosFactura datos = facturas.buscar(servicioId);
        if (datos == null) {
            throw new IllegalArgumentException("La orden " + servicioId + " no existe.");
        }
        if (!datos.sePuedeFacturar()) {
            throw new IllegalArgumentException("La orden " + servicioId + " está en estado '"
                    + datos.getEstado() + "'. Solo se factura cuando está 'Completado'.");
        }
        return datos;
    }

    /**
     * Si el usuario escribe mal la fecha, lanza IllegalArgumentException con un mensaje claro,
     * para que el menú muestre el error en vez de cerrarse.
     */
    private LocalDate convertirFecha(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("Escribe una fecha, por ejemplo 2026-10-01");
        }
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha inválida: '" + texto + "'. Escríbela así: 2026-10-01");
        }
    }
}
