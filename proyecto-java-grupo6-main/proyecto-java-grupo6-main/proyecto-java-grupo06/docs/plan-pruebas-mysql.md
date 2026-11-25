# Plan de pruebas con MySQL

## 1. Preparar una base de pruebas

Usa una base distinta de la de trabajo, porque las pruebas crean órdenes, repuestos y facturas.
Los scripts tienen `USE carmotors;`. Para usar otra base, cambia ese nombre en una copia de los
scripts o ejecútalos en una instalación de prueba.

Orden de carga:

1. `database/schema.sql` (todas las tablas, incluidas lotes, evaluaciones y reservas)
2. `database/data.sql` (datos de ejemplo y datos de las pruebas de reportes)

En IntelliJ, en *Run → Edit Configurations*, pon en *Environment variables*:

```
CARMOTORS_DB_URL=jdbc:mysql://localhost:3306/<base>;CARMOTORS_DB_USER=<usuario>;CARMOTORS_DB_PASSWORD=<clave>
```

## 2. Pruebas automáticas (Program arguments: `--mysql`)

Ejecútalas en este orden. `CicloFacturaPrueba` crea órdenes con la fecha de hoy y cambiaría
los conteos de las pruebas de reportes.

| # | Clase | Resultado esperado | Resultado (18-11-2026) |
|---|---|---|---|
| 1 | `ReportesDia2Prueba --mysql` | Reportes por nombre de servicio, tipo, estado, mes, productividad y consumo | ✅ OK (sin el reporte por nombre; falta repetir) |
| 2 | `ReportesDia3Prueba --mysql` | Datos de factura de la orden "batería descargada": subtotal 410.000 | ✅ OK |
| 3 | `CicloFacturaPrueba --mysql` | Reservas, ciclo, stock, factura, `.txt` y entrega | ✅ OK |
| 4 | `OrdenServicioPrueba --mysql` | Creación, técnicos, rollback y consola | ✅ OK |

## 3. Recorrido manual por consola

Anota lo que salió en cada paso. Los ID dependen de tu base.

| # | Menú | Acción | Esperado | Resultado (18-11-2026) |
|---|---|---|---|---|
| 1 | 1 | Registrar un cliente y su vehículo | Se guardan; un documento o una placa repetidos se rechazan | |
| 2 | 2 → 5 | Registrar un técnico | Devuelve su ID | |
| 3 | 3 | Revisar el stock de "Pastillas de freno" | Anotar el stock inicial (10 en `data.sql`) | 10 |
| 4 | 2 → 1 | Crear una orden para el vehículo (mano de obra 60000) | "Orden creada: N", estado Pendiente | ✅ orden 10 |
| 5 | 2 → 4 | Asignar el técnico | "Técnico asignado." | |
| 6 | 2 → 7 | Agregar 2 pastillas a la orden | "Repuesto reservado…"; el stock **sigue en 10** | ✅ |
| 7 | 2 → 7 | En otra orden, pedir más de lo disponible (10 − 2 = 8, pide 9) | Rechazado: "Stock insuficiente…" | ✅ (en `CicloFacturaPrueba`) |
| 8 | 4 → 1 | Vista previa de la orden en Pendiente | Rechazado: solo se factura en Completado | ✅ (en `CicloFacturaPrueba`) |
| 9 | 2 → 9 | Avanzar estado | "pasó a 'En Proceso'" | ✅ |
| 10 | 2 → 9 | Avanzar estado | "pasó a 'Completado'"; stock **baja a 8** | ✅ stock 8 |
| 11 | 2 → 9 | Avanzar otra vez | Rechazado: "Para pasarla a Facturado emite la factura…"; el stock sigue en 8 | ✅ |
| 12 | 2 → 7 | Agregar otro repuesto a la orden Completado | Rechazado | ✅ (en `CicloFacturaPrueba`) |
| 13 | 4 → 2 | Emitir factura, responder `n` | "No se emitió la factura." y la orden sigue Completado | ✅ |
| 14 | 4 → 2 | Emitir factura, responder `s` | Factura N.º X, total $297.500 y ruta del `.txt` | ✅ factura 2 |
| 15 | — | Abrir el `.txt` | Cliente, placa, 2 pastillas, subtotal 250.000, IVA 47.500 y total 297.500 | ✅ ver `docs/ejemplo-factura.txt` |
| 16 | 4 → 2 | Emitir otra vez la misma orden | Rechazado: ya está Facturado | ✅ (en `CicloFacturaPrueba`) |
| 17 | 4 → 3 | Borrar el `.txt` y regenerarlo | Se crea igual | ✅ (en `CicloFacturaPrueba`) |
| 18 | 2 → 9 | Avanzar estado | "pasó a 'Entregado'"; la consulta muestra la fecha de entrega | ✅ (en `CicloFacturaPrueba`) |
| 19 | 5 → 3 | Consumo de repuestos de hoy a hoy | Aparecen las 2 pastillas de la orden | |
| 20 | 5 → 2 | Productividad de hoy a hoy | Aparece el técnico con su orden terminada | |

## 4. Consultas para revisar la base

```sql
SELECT id, cantidad_stock FROM repuestos WHERE nombre = 'Pastillas de freno';
SELECT * FROM reservas_repuesto WHERE servicio_id = <orden>;     -- debe estar DESCONTADO
SELECT * FROM servicios_repuestos WHERE servicio_id = <orden>;   -- cantidad_usada y precio
SELECT * FROM facturas WHERE servicio_id = <orden>;              -- una sola fila
SELECT s.id, e.nombre, s.fecha_entrega FROM servicios s
  JOIN servicio_estados e ON e.id = s.estado_id WHERE s.id = <orden>;
```
