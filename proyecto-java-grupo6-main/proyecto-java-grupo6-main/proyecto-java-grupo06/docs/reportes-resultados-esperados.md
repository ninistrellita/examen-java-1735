# Reportes gerenciales — resultados esperados

Módulo de reportes de CarMotors.

## Archivos

| Archivo | Para qué sirve |
|---|---|
| `servicio/ReporteServicio.java` | Calcula los 3 reportes con Streams (día 2). Servicios más solicitados por nombre (día 5) |
| `modelo/CatalogoServicios.java` | La lista de servicios que se pueden pedir; la comparten órdenes y reportes (día 5) |
| `dao/ReporteDao.java` | Lee de MySQL las órdenes, técnicos y repuestos (día 2) |
| `controlador/ReporteControlador.java` | Recibe las fechas que escribe el usuario y llama al servicio (día 3) |
| `modelo/DatosFactura.java` | Datos que se le entregan a facturación (día 3) |
| `dao/FacturaDao.java` | Lee de MySQL los datos de la factura de una orden (día 3) |
| `modelo/Factura.java` | Calcula el IVA y el total final de la factura (día 4) |
| `servicio/FacturaServicio.java` | Emite la factura: la guarda, pasa la orden a Facturado y escribe el `.txt` (día 4) |
| `servicio/FacturaTexto.java` | Arma el texto de la factura (día 4) |
| `vista/MenuFacturacion.java` | Menú de facturación: vista previa, emitir y volver a generar el `.txt` (día 4) |
| `database/data.sql` | Datos de prueba. Se puede ejecutar varias veces |
| `test/.../ReportesDia2Prueba.java`, `ReportesDia3Prueba.java` y `CicloFacturaPrueba.java` | Pruebas |

## Reglas

- El periodo incluye el día inicial y el día final completos.
- Se usa la fecha en que se recibió la orden.
- Una orden está **terminada** si su estado es `Completado`, `Facturado` o `Entregado`.
- Una orden sin tipo o sin estado sale como `Sin tipo` / `Sin estado`.
- **Servicios más solicitados** cuenta por el nombre del servicio, que siempre es uno de la lista
  `modelo/CatalogoServicios` (en el menú de órdenes se elige por número). Sale de más a menos; con la
  misma cantidad, en el orden de la lista. Los servicios de la lista sin órdenes salen con 0.
- Una orden sin nombre (creada antes de la lista) sale como `Sin nombre (órdenes antiguas)`, y una con
  un nombre que no está en la lista, como `Otro (fuera de la lista)`. Esos dos grupos solo aparecen si hay órdenes.
- Solo se factura una orden en estado `Completado`.

## Datos de prueba

| Orden | Servicio | Fecha | Tipo | Estado | Técnicos | Repuestos | Mano de obra |
|---|---|---|---|---|---|---|---|
| Reporte: cambio de aceite | Cambio de aceite | 2026-10-05 | Preventivo | Entregado | Luis | — | 45.000 |
| Reporte: cambio de pastillas | Revisión de frenos | 2026-10-12 | Correctivo | Facturado | Luis, Ana | 2 pastillas × 95.000 | 80.000 |
| Reporte: batería descargada | Cambio de batería | 2026-10-20 | Correctivo | Completado | Ana | 1 batería × 380.000 | 30.000 |
| Reporte: revisión general | Revisión de frenos | 2026-11-03 | Preventivo | En Proceso | Luis | 1 pastilla × 95.000 | 60.000 |
| Reporte: ruido en la suspensión | Alineación | 2026-11-10 | Correctivo | Pendiente | Mario | — | 50.000 |

## Resultados esperados (del 2026-10-01 al 2026-11-30)

**Servicios más solicitados (menú de reportes, opción 1)**

| Servicio | Órdenes |
|---|---|
| Revisión de frenos | 2 |
| Cambio de aceite | 1 |
| Alineación | 1 |
| Cambio de batería | 1 |
| Balanceo | 0 |
| Revisión del sistema eléctrico | 0 |

Total: 5 órdenes.

**Servicios por tipo:** Correctivo 3, Preventivo 2

**Servicios por estado:** 1 de cada uno (Completado, En Proceso, Entregado, Facturado, Pendiente)

**Servicios por mes:** octubre 3, noviembre 2

**Productividad por técnico**

| Técnico | Asignadas | Terminadas | % |
|---|---|---|---|
| Ana Torres | 2 | 2 | 100 % |
| Luis Pérez | 3 | 2 | 67 % |
| Mario Díaz | 1 | 0 | 0 % |

**Consumo de repuestos**

| Repuesto | Unidades | Valor | Órdenes |
|---|---|---|---|
| Pastillas de freno | 3 | 285.000 | 2 |
| Batería 12V | 1 | 380.000 | 1 |

**Factura de "Reporte: batería descargada"**: repuestos 380.000 + mano de obra 30.000 = **410.000** (sin impuestos)

## Casos límite que se prueban

| Caso | Qué debe pasar |
|---|---|
| Periodo sin órdenes | Los reportes salen vacíos, sin errores |
| Un solo día | Solo salen las órdenes de ese día |
| Orden a las 23:59 del último día | Se incluye |
| Orden sin tipo, estado ni técnicos | Sale como "Sin tipo" / "Sin estado" y no cambia la productividad |
| Fecha vacía, mes 13 o formato `01/10/2026` | Se rechaza con un mensaje claro |
| Fecha inicial después de la final | Se rechaza |
| Factura sin repuestos | El subtotal es solo la mano de obra |
| Factura de una orden Pendiente o ya Facturada | No se permite |

## Datos que recibe facturación

`ReporteControlador.datosFactura(numeroDeOrden)` devuelve un `DatosFactura` con:

- número y estado de la orden
- nombre y documento del cliente
- placa del vehículo
- lista de repuestos usados (nombre, cantidad, precio)
- `getTotalRepuestos()` → va en la columna `total_repuestos`
- `getManoObra()` → va en la columna `total_mano_obra`
- `getSubtotal()` → repuestos + mano de obra, la base para calcular los impuestos

`FacturaServicio` calcula el IVA (19 %) y el total final, guarda la factura en `facturas`,
cambia la orden a `Facturado` en la misma transacción y escribe el `.txt` en la carpeta `facturas/`.

Los repuestos que se agregan a una orden desde la app (menú de órdenes, opción 7) se guardan en
`servicios_repuestos` cuando la orden pasa a `Completado`. Desde ese momento salen en la factura
y en el reporte de consumo.

## Cómo probar

Desde IntelliJ, ejecuta el `main` de `ReportesDia2Prueba`, `ReportesDia3Prueba` y `CicloFacturaPrueba`.

- **Sin argumentos:** cálculos y validaciones, sin base de datos.
- **Con `--mysql`** (en *Program arguments*): en una base nueva, carga `schema.sql` y después
  `data.sql`.
  Configura `CARMOTORS_DB_URL`, `CARMOTORS_DB_USER` y `CARMOTORS_DB_PASSWORD` en *Environment variables*.

Corre primero las pruebas de reportes y después `CicloFacturaPrueba`: esta crea órdenes con la
fecha de hoy y cambiaría los conteos de octubre y noviembre de 2026.
