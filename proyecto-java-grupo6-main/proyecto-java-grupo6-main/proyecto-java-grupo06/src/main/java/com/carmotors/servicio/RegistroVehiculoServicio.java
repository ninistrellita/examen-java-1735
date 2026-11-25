package com.carmotors.servicio;

import com.carmotors.dao.VehiculoDao;
import com.carmotors.modelo.Vehiculo;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Locale;

public class RegistroVehiculoServicio {
    private final VehiculoDao vehiculos;

    public RegistroVehiculoServicio(VehiculoDao vehiculos) {
        this.vehiculos = vehiculos;
    }

    public void registrarVehiculo(String documento, String placa, String marca, String modelo) throws SQLException {
        Vehiculo vehiculo = new Vehiculo(ValidacionRegistro.obligatorio(documento, 20, "Documento"),
                ValidacionRegistro.obligatorio(placa, 10, "Placa").toUpperCase(Locale.ROOT),
                ValidacionRegistro.textoConLetras(marca, 50, "Marca"),
                ValidacionRegistro.obligatorio(modelo, 50, "Modelo"));
        try {
            if (!vehiculos.insertar(vehiculo)) {
                throw new IllegalArgumentException("Primero registra al cliente.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("La placa ya está registrada o el cliente dejó de estar disponible.", e);
        }
    }
}
