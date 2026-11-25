package com.carmotors.dao;

import com.carmotors.config.ConexionFactory;
import com.carmotors.modelo.Repuesto;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcRepuestoDao {
    public int insertar(Repuesto repuesto) throws SQLException {
        String sql = "INSERT INTO repuestos (nombre, repuesto_tipo_id, proveedor_id, "
                + "precio_compra, precio_venta, cantidad_stock, stock_minimo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (var conexion = ConexionFactory.getInstancia().abrir();
             var consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            consulta.setString(1, repuesto.getNombre());
            consulta.setInt(2, repuesto.getRepuestoTipoId());
            consulta.setInt(3, repuesto.getProveedorId());
            consulta.setBigDecimal(4, repuesto.getPrecioCompra());
            consulta.setBigDecimal(5, repuesto.getPrecioVenta());
            consulta.setInt(6, repuesto.getCantidadStock());
            consulta.setInt(7, repuesto.getStockMinimo());
            consulta.executeUpdate();
            try (var ids = consulta.getGeneratedKeys()) {
                if (ids.next()) {
                    repuesto.setId(ids.getInt(1));
                    return repuesto.getId();
                }
                throw new SQLException("MySQL no devolvió el ID del repuesto.");
            }
        }
    }

    public Optional<Repuesto> buscarPorId(int id) throws SQLException {
        return buscar("WHERE id = ?", id).stream().findFirst();
    }

    public List<Repuesto> listarTodos() throws SQLException {
        return buscar("", null);
    }

    public List<Repuesto> listarPorProveedor(int proveedorId) throws SQLException {
        return buscar("WHERE proveedor_id = ?", proveedorId);
    }

    public List<Repuesto> listarBajoStockMinimo() throws SQLException {
        return buscar("WHERE cantidad_stock < stock_minimo", null);
    }

    public void sumarStock(int id, int cantidad) throws SQLException {
        escribirStock("cantidad_stock + ?", id, cantidad);
    }

    public void actualizarStock(int id, int cantidad) throws SQLException {
        escribirStock("?", id, cantidad);
    }

    private List<Repuesto> buscar(String filtro, Integer id) throws SQLException {
        List<Repuesto> repuestos = new ArrayList<>();
        try (var conexion = ConexionFactory.getInstancia().abrir();
             var consulta = conexion.prepareStatement("SELECT * FROM repuestos " + filtro + " ORDER BY id")) {
            if (id != null) {
                consulta.setInt(1, id);
            }
            try (var filas = consulta.executeQuery()) {
                while (filas.next()) {
                    repuestos.add(new Repuesto(filas.getInt("id"), filas.getString("nombre"), null,
                            filas.getInt("repuesto_tipo_id"), filas.getInt("proveedor_id"),
                            filas.getBigDecimal("precio_compra"), filas.getBigDecimal("precio_venta"),
                            filas.getInt("cantidad_stock"), filas.getInt("stock_minimo")));
                }
            }
        }
        return repuestos;
    }

    private void escribirStock(String expresion, int id, int cantidad) throws SQLException {
        String sql = "UPDATE repuestos SET cantidad_stock = " + expresion + " WHERE id = ?";
        try (var conexion = ConexionFactory.getInstancia().abrir();
             var consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, cantidad);
            consulta.setInt(2, id);
            if (consulta.executeUpdate() != 1) {
                throw new SQLException("No se pudo actualizar el repuesto " + id);
            }
        }
    }
}
