package com.carmotors;

import com.carmotors.config.ConexionFactory;
import com.carmotors.controlador.FacturaControlador;
import com.carmotors.controlador.InventarioControlador;
import com.carmotors.controlador.ReporteControlador;
import com.carmotors.dao.JdbcProveedorDao;
import com.carmotors.dao.JdbcRepuestoDao;
import com.carmotors.dao.LoteDao;
import com.carmotors.dao.EvaluacionProveedorDao;
import com.carmotors.dao.ReservaRepuestoDao;
import com.carmotors.dao.ReporteDao;
import com.carmotors.dao.FacturaDao;
import com.carmotors.servicio.FacturaServicio;
import com.carmotors.servicio.ReporteServicio;
import com.carmotors.servicio.InventarioProveedorServicio;
import com.carmotors.vista.MenuFacturacion;
import com.carmotors.vista.MenuReportes;
import com.carmotors.vista.MenuInventario;
import com.carmotors.controlador.ClienteControlador;
import com.carmotors.controlador.OrdenServicioControlador;
import com.carmotors.servicio.OrdenServicioServicio;
import com.carmotors.dao.ClienteDao;
import com.carmotors.dao.HistorialClienteDao;
import com.carmotors.dao.VehiculoDao;
import com.carmotors.servicio.HistorialClienteServicio;
import com.carmotors.servicio.RegistroClienteServicio;
import com.carmotors.servicio.RegistroVehiculoServicio;
import com.carmotors.vista.MenuClientes;
import com.carmotors.vista.MenuPrincipal;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        RegistroClienteServicio registro = new RegistroClienteServicio(new ClienteDao());
        HistorialClienteServicio consultas = new HistorialClienteServicio(new HistorialClienteDao());
        ClienteControlador clientes = new ClienteControlador(registro, new RegistroVehiculoServicio(new VehiculoDao()), consultas);
        OrdenServicioServicio ordenes = new OrdenServicioServicio(ConexionFactory.getInstancia()::abrir);
        ordenes.suscribir(aviso -> System.out.printf("Aviso para %s | Orden %d | Estado %s%n",
                aviso.correo(), aviso.orden(), aviso.estado()));
        InventarioProveedorServicio inventario = new InventarioProveedorServicio(new JdbcProveedorDao(),
                new JdbcRepuestoDao(), new LoteDao(), new EvaluacionProveedorDao(), new ReservaRepuestoDao());
        ReporteControlador reportes = new ReporteControlador(new ReporteServicio(new ReporteDao()), new FacturaDao());
        // Las facturas .txt se guardan en la carpeta "facturas" (junto a donde se ejecuta el programa)
        FacturaServicio facturacion = new FacturaServicio(ConexionFactory.getInstancia()::abrir, new FacturaDao(),
                ordenes, Path.of("facturas"));
        new MenuPrincipal(new MenuClientes(clientes), new OrdenServicioControlador(ordenes),
                new MenuInventario(new InventarioControlador(inventario)),
                new MenuFacturacion(new FacturaControlador(facturacion)), new MenuReportes(reportes)).ejecutar();
    }
}
