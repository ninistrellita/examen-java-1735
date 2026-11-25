package com.carmotors.controlador;

import com.carmotors.modelo.EvaluacionProveedor;
import com.carmotors.modelo.Proveedor;
import com.carmotors.modelo.Repuesto;
import com.carmotors.modelo.ReservaRepuesto;
import com.carmotors.modelo.Lote;
import com.carmotors.servicio.InventarioProveedorServicio;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class InventarioControlador {

    private final InventarioProveedorServicio servicio;

    public InventarioControlador(InventarioProveedorServicio servicio) {
        this.servicio = servicio;
    }

    public int registrarProveedor(String nombre, String contacto) throws SQLException {
        return servicio.registrarProveedor(nombre, contacto);
    }

    public List<Proveedor> listarProveedores() throws SQLException {
        return servicio.listarProveedores();
    }

    public int registrarRepuesto(String nombre, String marca, int tipoId, int proveedorId,
                                 BigDecimal precioCompra, BigDecimal precioVenta,
                                 int stock, int stockMinimo) throws SQLException {
        return servicio.registrarRepuesto(nombre, marca, tipoId, proveedorId,
                precioCompra, precioVenta, stock, stockMinimo);
    }

    public List<Repuesto> catalogo(int proveedorId) throws SQLException {
        return servicio.catalogoPorProveedor(proveedorId);
    }

    public List<Repuesto> listarRepuestos() throws SQLException {
        return servicio.listarRepuestos();
    }

    public int registrarLote(String codigo, int repuestoId, int proveedorId,
                             LocalDate ingreso, LocalDate caducidad, int cantidad) throws SQLException {
        return servicio.registrarLote(codigo, repuestoId, proveedorId, ingreso, caducidad, cantidad);
    }

    public int registrarEvaluacion(int proveedorId, int calidad, int puntualidad,
                                   String observaciones) throws SQLException {
        return servicio.registrarEvaluacion(proveedorId, calidad, puntualidad, observaciones);
    }

    public List<EvaluacionProveedor> evaluaciones(int proveedorId) throws SQLException {
        return servicio.evaluacionesDe(proveedorId);
    }

    public List<String> alertas() throws SQLException {
        return servicio.generarReporteAlertas();
    }

    public ReservaRepuesto reservar(int repuestoId, int cantidad, int servicioId) throws SQLException {
        return servicio.reservar(repuestoId, cantidad, servicioId);
    }

    public void descontarStock(int servicioId) throws SQLException {
        servicio.descontarStock(servicioId);
    }

    public void cancelarReservas(int servicioId) throws SQLException {
        servicio.cancelarReservas(servicioId);
    }

    public List<ReservaRepuesto> reservasDeServicio(int servicioId) throws SQLException {
        return servicio.reservasDeServicio(servicioId);
    }
    public List<Lote> lotesPorRepuesto(int repuestoId) throws SQLException {
        return servicio.lotesPorRepuesto(repuestoId);
    }
}
