package com.carmotors.dao;

import java.sql.*;
import java.util.*;

public class TecnicoOrdenDao {
    public void asignar(Connection c, int ordenId, int tecnicoId) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "INSERT INTO servicios_tecnicos (servicio_id, tecnico_id) VALUES (?, ?)")) {
            p.setInt(1, ordenId);
            p.setInt(2, tecnicoId);
            p.executeUpdate();
        }
    }

    public List<Integer> listar(Connection c, int ordenId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement p = c.prepareStatement(
                "SELECT tecnico_id FROM servicios_tecnicos WHERE servicio_id = ? ORDER BY tecnico_id")) {
            p.setInt(1, ordenId);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) ids.add(r.getInt(1));
            }
        }
        return List.copyOf(ids);
    }
}
