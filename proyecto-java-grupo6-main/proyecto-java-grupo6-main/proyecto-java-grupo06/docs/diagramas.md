# Diagramas de CarMotors (proyecto integrado)

GitHub dibuja estos diagramas automáticamente. Para tener los `.png`, pega cada bloque en
<https://mermaid.live>, exporta la imagen y reemplaza `diagrama-er.png`, `diagrama-clases.png`
y `arquitectura.png` con el mismo nombre, para que los enlaces del README sigan funcionando.

## 1. Entidad-relación

```mermaid
erDiagram
    clientes ||--o{ vehiculos : "tiene"
    vehiculos ||--o{ servicios : "recibe"
    mantenimiento_tipos ||--o{ servicios : "clasifica"
    servicio_estados ||--o{ servicios : "estado"
    servicios ||--o{ servicios_tecnicos : ""
    tecnicos ||--o{ servicios_tecnicos : ""
    servicios ||--o{ servicios_repuestos : "consume"
    repuestos ||--o{ servicios_repuestos : ""
    servicios ||--o{ reservas_repuesto : "reserva"
    repuestos ||--o{ reservas_repuesto : ""
    servicios ||--o| facturas : "se factura en"
    repuesto_tipos ||--o{ repuestos : "clasifica"
    proveedores ||--o{ repuestos : "suministra"
    proveedores ||--o{ lotes : "entrega"
    repuestos ||--o{ lotes : ""
    proveedores ||--o{ evaluaciones_proveedor : "se evalúa"

    clientes {
        int id PK
        varchar nombre
        varchar documento_identidad UK
        varchar telefono
        varchar email UK
    }
    vehiculos {
        int id PK
        int cliente_id FK
        varchar placa UK
        varchar marca
        varchar modelo
    }
    servicios {
        int id PK
        int vehiculo_id FK
        int mantenimiento_tipo_id FK
        int estado_id FK
        datetime fecha_recepcion
        datetime fecha_entrega
        text descripcion_problema
        decimal costo_mano_obra
    }
    tecnicos {
        int id PK
        varchar nombre
    }
    servicios_tecnicos {
        int servicio_id PK
        int tecnico_id PK
    }
    repuestos {
        int id PK
        varchar nombre
        int repuesto_tipo_id FK
        int proveedor_id FK
        decimal precio_compra
        decimal precio_venta
        int cantidad_stock
        int stock_minimo
    }
    servicios_repuestos {
        int servicio_id PK
        int repuesto_id PK
        int cantidad_usada
        decimal precio_unitario_venta
    }
    reservas_repuesto {
        int id PK
        int repuesto_id FK
        int servicio_id
        int cantidad
        varchar estado "RESERVADO, DESCONTADO, CANCELADO"
        datetime fecha_reserva
    }
    facturas {
        int id PK
        int servicio_id FK
        datetime fecha_emision
        decimal total_repuestos
        decimal total_mano_obra
        decimal impuestos
        decimal total_final
    }
    proveedores {
        int id PK
        varchar nombre
        varchar contacto
    }
    lotes {
        int id PK
        varchar codigo_lote UK
        int repuesto_id FK
        int proveedor_id FK
        date fecha_ingreso
        date fecha_caducidad
        int cantidad_recibida
        int cantidad_disponible
    }
    evaluaciones_proveedor {
        int id PK
        int proveedor_id FK
        date fecha_evaluacion
        tinyint calificacion_calidad
        tinyint calificacion_puntualidad
        text observaciones
    }
    servicio_estados {
        int id PK
        varchar nombre UK
    }
    mantenimiento_tipos {
        int id PK
        varchar nombre UK
    }
    repuesto_tipos {
        int id PK
        varchar nombre UK
    }
```

## 2. Ciclo de estados de una orden

```mermaid
stateDiagram-v2
    [*] --> Pendiente : crear orden
    Pendiente --> EnProceso : avanzar (menú 2, opción 9)
    EnProceso --> Completado : avanzar - descuenta el stock reservado
    Completado --> Facturado : emitir factura (menú 4) - guarda la factura y el .txt
    Facturado --> Entregado : avanzar - guarda la fecha de entrega
    Entregado --> [*]
    EnProceso : En Proceso
    note right of Pendiente : Pendiente y En Proceso admiten repuestos (se reservan)
```

## 3. Arquitectura por capas (MVC + servicio + DAO)

```mermaid
flowchart TB
    subgraph Vista
        MP[MenuPrincipal] --> MC[MenuClientes]
        MP --> MO[MenuOrdenes]
        MP --> MI[MenuInventario]
        MP --> MF[MenuFacturacion]
        MP --> MR[MenuReportes]
    end
    subgraph Controlador
        CC[ClienteControlador]
        OC[OrdenServicioControlador]
        IC[InventarioControlador]
        FC[FacturaControlador]
        RC[ReporteControlador]
    end
    subgraph Servicio
        RS[RegistroClienteServicio / HistorialClienteServicio / RegistroVehiculoServicio]
        OS[OrdenServicioServicio]
        IS[InventarioProveedorServicio]
        FS[FacturaServicio]
        REP[ReporteServicio]
    end
    subgraph DAO
        D1[ClienteDao / VehiculoDao / HistorialClienteDao]
        D2[OrdenServicioDao / EstadoServicioDao / TecnicoOrdenDao / RepuestoOrdenDao]
        D3[JdbcRepuestoDao / JdbcProveedorDao / LoteDao / ReservaRepuestoDao / EvaluacionProveedorDao]
        D4[FacturaDao]
        D5[ReporteDao]
    end
    MC --> CC --> RS --> D1
    MO --> OC --> OS --> D2
    MI --> IC --> IS --> D3
    MF --> FC --> FS --> D4
    FS --> OS
    MR --> RC --> REP --> D5
    D1 & D2 & D3 & D4 & D5 --> CF[("ConexionFactory<br>Singleton + Factory")]
    CF --> DB[(MySQL)]
```

## 4. Clases del ciclo de la orden y la facturación

```mermaid
classDiagram
    class MenuOrdenes {
        +mostrar()
   
    }
    class MenuFacturacion {
        +ejecutar(Scanner)
   
    }
    class OrdenServicioControlador {
        +agregarRepuesto(orden, repuesto, cantidad)
        +repuestosDe(orden) List~ReservaRepuesto~
        +avanzarEstado(orden) String
        +estadoDe(orden) String
   
    }
    class FacturaControlador {
        +vistaPrevia(orden) Factura
        +emitir(orden) FacturaEmitida
        +regenerarTexto(orden) FacturaEmitida
   
    }
    class OrdenServicioServicio {
        +crear(...) int
        +agregarRepuesto(orden, repuesto, cantidad)
        +avanzarEstado(orden) String
        +marcarFacturada(Connection, orden)
        -aplicar(Connection, orden, nuevo)
        -enTransaccion(Trabajo) T
   
    }
    class FacturaServicio {
        +vistaPrevia(orden) Factura
        +emitir(orden) FacturaEmitida
        +regenerarTexto(orden) FacturaEmitida
   
    }
    class FacturaTexto {
        +generar(Factura)$ String
   
    }
    class CicloOrden {
        +siguiente(estado)$ Optional~String~
        +admiteRepuestos(estado)$ boolean
   
    }
    class Factura {
        +TASA_IVA$ BigDecimal
        +calcular(DatosFactura, fecha)$ Factura
        +conNumero(int) Factura
        +getImpuestos() BigDecimal
        +getTotal() BigDecimal
   
    }
    class DatosFactura {
        +getSubtotal() BigDecimal
        +sePuedeFacturar() boolean
   
    }
    class OrdenServicioDao {
        +buscar(Connection, id, bloquear)
        +actualizarEstado(Connection, id, estadoId, fechaEntrega)
   
    }
    class RepuestoOrdenDao {
        +bloquear(Connection, repuesto)
        +totalReservado(Connection, repuesto) int
        +reservar(Connection, orden, repuesto, cantidad)
        +reservasActivas(Connection, orden)
        +descontar(Connection, ReservaRepuesto)
   
    }
    class FacturaDao {
        +buscar(Connection, orden) DatosFactura
        +insertar(Connection, Factura) int
        +buscarEmitida(Connection, orden) Factura
   
    }
    MenuOrdenes --> OrdenServicioControlador
    MenuFacturacion --> FacturaControlador
    OrdenServicioControlador --> OrdenServicioServicio
    FacturaControlador --> FacturaServicio
    FacturaServicio --> OrdenServicioServicio : marcarFacturada
    FacturaServicio --> FacturaDao
    FacturaServicio --> FacturaTexto
    OrdenServicioServicio --> OrdenServicioDao
    OrdenServicioServicio --> RepuestoOrdenDao
    OrdenServicioServicio ..> CicloOrden
    Factura --> DatosFactura
    FacturaDao ..> Factura
```
