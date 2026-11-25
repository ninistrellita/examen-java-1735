package com.carmotors.dao;

import com.carmotors.modelo.EstadoServicio;
import java.sql.*;
import java.util.*;

/** Los estados son un catálogo de consulta, sin modificaciones desde la consola. */
public class EstadoServicioDao {
    public List<EstadoServicio> listar(Connection c) throws SQLException {
        List<EstadoServicio> estados = new ArrayList<>();
        try (PreparedStatement p = c.prepareStatement("SELECT id, nombre FROM servicio_estados ORDER BY id");
             ResultSet r = p.executeQuery()) {
            while (r.next()) estados.add(new EstadoServicio(r.getInt(1), r.getString(2)));
        }
        return List.copyOf(estados);
    }

    public EstadoServicio buscar(Connection c, int id) throws SQLException {
        return listar(c).stream().filter(e -> e.getId() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado inexistente: " + id));
    }

    public int idPorNombre(Connection c, String nombre) throws SQLException {
        return listar(c).stream().filter(e -> e.getNombre().equals(nombre)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Falta el estado: " + nombre)).getId();
    }
}
