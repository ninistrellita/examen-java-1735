package com.carmotors.dao;

import com.carmotors.config.ConexionFactory;
import com.carmotors.modelo.Vehiculo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VehiculoDao {
    public boolean insertar(Vehiculo vehiculo) throws SQLException {
        String sql = "INSERT INTO vehiculos (cliente_id, placa, marca, modelo) "
                + "SELECT id, ?, ?, ? FROM clientes WHERE documento_identidad = ? ";
        try (Connection conexion = ConexionFactory.getInstancia().abrir();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, vehiculo.getPlaca());
            consulta.setString(2, vehiculo.getMarca());
            consulta.setString(3, vehiculo.getModelo());
            consulta.setString(4, vehiculo.getDocumentoCliente());
            return consulta.executeUpdate() == 1;
        }
    }
}
