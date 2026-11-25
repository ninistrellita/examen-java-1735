package com.carmotors.controlador;

import com.carmotors.modelo.HistorialVehiculo;
import com.carmotors.servicio.HistorialClienteServicio;
import com.carmotors.servicio.HistorialClienteServicio.Recordatorio;
import com.carmotors.servicio.RegistroClienteServicio;
import com.carmotors.servicio.RegistroVehiculoServicio;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ClienteControlador {
    private final RegistroClienteServicio registro;
    private final HistorialClienteServicio consultas;
    private final RegistroVehiculoServicio vehiculos;

    public ClienteControlador(RegistroClienteServicio registro, HistorialClienteServicio consultas) {
        this.vehiculos = null;
        this.registro = registro;
        this.consultas = consultas;
    }

    public ClienteControlador(RegistroClienteServicio registro, RegistroVehiculoServicio vehiculos,
                              HistorialClienteServicio consultas) {
        this.registro = registro;
        this.vehiculos = vehiculos;
        this.consultas = consultas;
    }
    public void registrarCliente(String nombre, String documento, String telefono, String email) throws SQLException {
        registro.registrarCliente(nombre, documento, telefono, email);
    }

    public void registrarVehiculo(String documento, String placa, String marca, String modelo) throws SQLException {
        if (vehiculos == null) {
            registro.registrarVehiculo(documento, placa, marca, modelo);
        } else {
            vehiculos.registrarVehiculo(documento, placa, marca, modelo);
        }
    }

    public List<HistorialVehiculo> historial(String documento) throws SQLException {
        return consultas.historial(documento);
    }

    public List<Recordatorio> recordatorios() throws SQLException {
        return consultas.recordatorios(LocalDate.now());
    }
}
