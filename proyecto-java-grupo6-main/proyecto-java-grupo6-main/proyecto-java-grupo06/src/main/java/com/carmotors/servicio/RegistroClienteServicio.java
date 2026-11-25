package com.carmotors.servicio;

import com.carmotors.dao.ClienteDao;
import com.carmotors.dao.VehiculoDao;
import com.carmotors.modelo.Cliente;
import com.carmotors.excepcion.CampoInvalidoException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Locale;

public class RegistroClienteServicio {
    private final ClienteDao clientes;
    private final RegistroVehiculoServicio registroVehiculos;

    public RegistroClienteServicio(ClienteDao clientes) {
        this(clientes, new VehiculoDao());
    }

    public RegistroClienteServicio(ClienteDao clientes, VehiculoDao vehiculos) {
        this.clientes = clientes;
        this.registroVehiculos = new RegistroVehiculoServicio(vehiculos);
    }

    public void registrarCliente(String nombre, String documento, String telefono, String email) throws SQLException {
        String correo = ValidacionRegistro.obligatorio(email, 100, "Correo").toLowerCase(Locale.ROOT);
        if (!correo.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new CampoInvalidoException("Correo", "Ingresa un correo válido.");
        }
        Cliente cliente = new Cliente(ValidacionRegistro.nombrePersona(nombre, "Nombre"),
                ValidacionRegistro.digitos(documento, 20, "Documento", false), ValidacionRegistro.digitos(telefono, 20, "Teléfono", false), correo);
        try {
            clientes.insertar(cliente);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("El documento o correo ya está registrado.", e);
        }
    }

    public void registrarVehiculo(String documento, String placa, String marca, String modelo) throws SQLException {
        registroVehiculos.registrarVehiculo(documento, placa, marca, modelo);
    }
}
