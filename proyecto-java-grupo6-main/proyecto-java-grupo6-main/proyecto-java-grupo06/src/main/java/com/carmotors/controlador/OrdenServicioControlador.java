package com.carmotors.controlador;

import com.carmotors.modelo.OrdenServicio;
import com.carmotors.modelo.ReservaRepuesto;
import com.carmotors.modelo.Tecnico;
import com.carmotors.servicio.OrdenServicioServicio;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class OrdenServicioControlador {
    private final OrdenServicioServicio servicio;

    public OrdenServicioControlador(OrdenServicioServicio servicio) {
        this.servicio = java.util.Objects.requireNonNull(servicio);
    }

    public int crear(int vehiculo, int tipo, String problema, BigDecimal manoObra) throws SQLException {
        return servicio.crear(vehiculo, tipo, problema, manoObra);
    }

    public int crear(int vehiculo, int tipo, String nombreServicio, String problema,
                     BigDecimal manoObra) throws SQLException {
        return servicio.crear(vehiculo, tipo, nombreServicio, problema, manoObra);
    }

    public OrdenServicio consultar(int id) throws SQLException { return servicio.consultar(id); }
    public List<OrdenServicio> listar() throws SQLException { return servicio.listar(); }
    public List<Integer> tecnicosDe(int orden) throws SQLException { return servicio.tecnicosDe(orden); }
    public void asignarTecnico(int orden, int tecnico) throws SQLException {
        servicio.asignarTecnico(orden, tecnico);
    }

    public int registrarTecnico(String nombre) throws SQLException {
        return servicio.registrarTecnico(nombre);
    }

    public List<Tecnico> listarTecnicos() throws SQLException {
        return servicio.listarTecnicos();
    }

    public List<String> serviciosSugeridos() {
        return servicio.serviciosSugeridos();
    }

    public String estadoDe(int orden) throws SQLException { return servicio.estadoDe(orden); }

    public void agregarRepuesto(int orden, int repuesto, int cantidad) throws SQLException {
        servicio.agregarRepuesto(orden, repuesto, cantidad);
    }

    public List<ReservaRepuesto> repuestosDe(int orden) throws SQLException { return servicio.repuestosDe(orden); }

    public String avanzarEstado(int orden) throws SQLException { return servicio.avanzarEstado(orden); }
}
