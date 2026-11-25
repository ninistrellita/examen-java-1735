package com.carmotors.servicio;

import com.carmotors.dao.EvaluacionProveedorDao;
import com.carmotors.dao.LoteDao;
import com.carmotors.dao.JdbcProveedorDao;
import com.carmotors.dao.JdbcRepuestoDao;
import com.carmotors.dao.ReservaRepuestoDao;
import com.carmotors.modelo.EvaluacionProveedor;
import com.carmotors.modelo.Lote;
import com.carmotors.modelo.Proveedor;
import com.carmotors.modelo.Repuesto;
import com.carmotors.modelo.ReservaRepuesto;
import com.carmotors.modelo.ReservaRepuesto.EstadoReserva;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class InventarioProveedorServicio {

    private final JdbcProveedorDao proveedores;
    private final JdbcRepuestoDao repuestos;
    private final LoteDao lotes;
    private final EvaluacionProveedorDao evaluaciones;
    private final ReservaRepuestoDao reservas;

    public InventarioProveedorServicio(JdbcProveedorDao proveedores, JdbcRepuestoDao repuestos,
                                       LoteDao lotes, EvaluacionProveedorDao evaluaciones,
                                       ReservaRepuestoDao reservas) {
        this.proveedores = proveedores;
        this.repuestos = repuestos;
        this.lotes = lotes;
        this.evaluaciones = evaluaciones;
        this.reservas = reservas;
    }


    public int registrarProveedor(String nombre, String contacto) throws SQLException {
        Proveedor p = new Proveedor(0, ValidacionRegistro.textoConLetras(nombre, 255, "Nombre del proveedor"),
                obligatorio(contacto, 255, "Contacto"));
        try {
            return proveedores.insertar(p);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("El proveedor ya existe o los datos no son válidos.", e);
        }
    }

    public List<Proveedor> listarProveedores() throws SQLException {
        return proveedores.listarTodos();
    }

    public Proveedor obtenerProveedor(int id) throws SQLException {
        return proveedores.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor id=" + id + " no encontrado."));
    }

    public int registrarRepuesto(String nombre, String marca, int tipoId, int proveedorId,
                                 BigDecimal precioCompra, BigDecimal precioVenta,
                                 int stock, int stockMinimo) throws SQLException {
        if (precioCompra == null || precioCompra.signum() < 0)
            throw new IllegalArgumentException("Precio de compra inválido.");
        if (precioVenta == null || precioVenta.signum() < 0)
            throw new IllegalArgumentException("Precio de venta inválido.");
        if (stock < 0 || stockMinimo < 0)
            throw new IllegalArgumentException("Stock y stock mínimo no pueden ser negativos.");
        obtenerProveedor(proveedorId);
        Repuesto r = new Repuesto(0, ValidacionRegistro.textoConLetras(nombre, 255, "Nombre del repuesto"),
                obligatorio(marca, 100, "Marca"), tipoId, proveedorId,
                precioCompra, precioVenta, stock, stockMinimo);
        try {
            return repuestos.insertar(r);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("No se pudo registrar el repuesto.", e);
        }
    }

    public List<Repuesto> catalogoPorProveedor(int proveedorId) throws SQLException {
        obtenerProveedor(proveedorId);
        return repuestos.listarPorProveedor(proveedorId);
    }

    public List<Repuesto> listarRepuestos() throws SQLException {
        return repuestos.listarTodos();
    }

    public Repuesto obtenerRepuesto(int id) throws SQLException {
        return repuestos.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Repuesto id=" + id + " no encontrado."));
    }

    public int registrarLote(String codigoLote, int repuestoId, int proveedorId,
                             LocalDate fechaIngreso, LocalDate fechaCaducidad,
                             int cantidadRecibida) throws SQLException {
        if (cantidadRecibida <= 0)
            throw new IllegalArgumentException("La cantidad recibida debe ser mayor a cero.");
        if (fechaIngreso == null) fechaIngreso = LocalDate.now();
        obtenerRepuesto(repuestoId);
        obtenerProveedor(proveedorId);
        Lote lote = new Lote(obligatorio(codigoLote, 50, "Código de lote"),
                repuestoId, proveedorId, fechaIngreso, fechaCaducidad, cantidadRecibida);
        try {
            int id = lotes.insertar(lote);
            repuestos.sumarStock(repuestoId, cantidadRecibida);
            return id;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("El código de lote ya existe o los datos no son válidos.", e);
        }
    }

    public List<Lote> lotesPorRepuesto(int repuestoId) throws SQLException {
        return lotes.listarPorRepuesto(repuestoId);
    }

    public int registrarEvaluacion(int proveedorId, int calidad, int puntualidad,
                                   String observaciones) throws SQLException {
        if (calidad < 1 || calidad > 5)
            throw new IllegalArgumentException("Calificación de calidad debe estar entre 1 y 5.");
        if (puntualidad < 1 || puntualidad > 5)
            throw new IllegalArgumentException("Calificación de puntualidad debe estar entre 1 y 5.");
        obtenerProveedor(proveedorId);
        return evaluaciones.insertar(new EvaluacionProveedor(0,
                proveedorId, LocalDate.now(), calidad, puntualidad,
                obligatorio(observaciones, 500, "Observaciones")));
    }

    public List<EvaluacionProveedor> evaluacionesDe(int proveedorId) throws SQLException {
        obtenerProveedor(proveedorId);
        return evaluaciones.listarPorProveedor(proveedorId);
    }

    public List<Repuesto> alertasStockBajo() throws SQLException {
        return repuestos.listarBajoStockMinimo();
    }

    public List<Lote> alertasCaducidad() throws SQLException {
        return lotes.listarProximosACaducar();
    }

    public List<String> generarReporteAlertas() throws SQLException {
        List<String> lineas = new ArrayList<>();
        List<Repuesto> bajos = alertasStockBajo();
        if (bajos.isEmpty()) {
            lineas.add("Sin alertas de stock bajo.");
        } else {
            lineas.add("=== ALERTA: Stock bajo mínimo ===");
            for (Repuesto r : bajos) {
                lineas.add(String.format("  - [%d] %s | stock=%d | mínimo=%d",
                        r.getId(), r.getNombre(), r.getCantidadStock(), r.getStockMinimo()));
            }
        }
        List<Lote> caduca = alertasCaducidad();
        if (caduca.isEmpty()) {
            lineas.add("Sin alertas de caducidad.");
        } else {
            lineas.add("=== ALERTA: Lotes por caducar / caducados ===");
            for (Lote l : caduca) {
                lineas.add(String.format("  - %s | repuestoId=%d | caduca=%s | disponible=%d%s",
                        l.getCodigoLote(), l.getRepuestoId(), l.getFechaCaducidad(),
                        l.getCantidadDisponible(),
                        l.estaCaducado() ? " [CADUCADO]" : ""));
            }
        }
        return lineas;
    }


    public ReservaRepuesto reservar(int repuestoId, int cantidad, int servicioId) throws SQLException {
        OrdenServicioServicio ordenes = new OrdenServicioServicio(
                com.carmotors.config.ConexionFactory.getInstancia()::abrir);
        ordenes.agregarRepuesto(servicioId, repuestoId, cantidad);
        return ordenes.repuestosDe(servicioId).stream()
                .filter(reserva -> reserva.getRepuestoId() == repuestoId && reserva.estaActiva())
                .findFirst().orElseThrow(() -> new IllegalStateException("No se encontró la reserva creada."));
    }

    public void descontarStock(int servicioId) throws SQLException {
        OrdenServicioServicio ordenes = new OrdenServicioServicio(
                com.carmotors.config.ConexionFactory.getInstancia()::abrir);
        String estado = ordenes.estadoDe(servicioId);
        if (com.carmotors.modelo.CicloOrden.EN_PROCESO.equals(estado)) {
            ordenes.avanzarEstado(servicioId);
        } else if (com.carmotors.modelo.CicloOrden.PENDIENTE.equals(estado)) {
            throw new IllegalArgumentException("Primero pasa la orden a En Proceso.");
        }
    }
    public void cancelarReservas(int servicioId) throws SQLException {
        for (ReservaRepuesto reserva : reservas.listarPorServicio(servicioId)) {
            if (reserva.getEstado() == EstadoReserva.RESERVADO) {
                reservas.actualizarEstado(reserva.getId(), EstadoReserva.CANCELADO);
            }
        }
    }

    public List<ReservaRepuesto> reservasDeServicio(int servicioId) throws SQLException {
        return reservas.listarPorServicio(servicioId);
    }

    private static String obligatorio(String valor, int limite, String campo) {
        String limpio = opcional(valor, limite, campo);
        if (limpio == null) throw new IllegalArgumentException(campo + " es obligatorio.");
        return limpio;
    }

    private static String opcional(String valor, int limite, String campo) {
        if (valor == null || valor.isBlank()) return null;
        String limpio = valor.strip();
        if (limpio.length() > limite)
            throw new IllegalArgumentException(campo + " admite hasta " + limite + " caracteres.");
        return limpio;
    }
}
