# Consulta de ofertas por cliente

`GET /api/v1/jobs` permite combinar los siguientes parámetros:

| Parámetro | Descripción | Valor por defecto |
| --- | --- | --- |
| `clientId` | UUID del cliente, obligatorio. | — |
| `createdFrom` | Fecha inicial de creación, inclusiva, formato `YYYY-MM-DD`. | Sin límite |
| `createdTo` | Fecha final de creación, incluye todo el día, formato `YYYY-MM-DD`. | Sin límite |
| `specialty` | Especialidad: nombre del enum o su descripción, por ejemplo `GASFITERO` o `Gasfitero`. | Todas |
| `minBudget` | Límite inferior inclusivo sobre el campo `maxBudget` de la oferta. | Sin límite |
| `maxBudget` | Límite superior inclusivo sobre el campo `maxBudget` de la oferta. | Sin límite |
| `status` | `OPEN`, `IN_PROGRESS`, `COMPLETED` o `CANCELED`. | Todos |
| `sortBy` | Campo de ordenamiento: `createdAt` o `maxBudget`. | `createdAt` |
| `sortDirection` | `ASC` (ascendente) o `DESC` (descendente). | `DESC` |
| `page` | Página, desde cero. | `0` |
| `size` | Tamaño de página, entre 1 y 50. | `10` |

Los filtros se aplican conjuntamente antes de paginar. Para consultar una sola fecha,
envía el mismo día en `createdFrom` y `createdTo`. Para consultar un presupuesto exacto,
envía el mismo importe en `minBudget` y `maxBudget`.

Ejemplo con ofertas de gasfitería abiertas, creadas durante noviembre, con presupuesto
entre 100 y 500, ordenadas de mayor a menor presupuesto:

```http
GET /api/v1/jobs?clientId=550e8400-e29b-41d4-a716-446655440000&createdFrom=2026-11-01&createdTo=2026-11-30&specialty=GASFITERO&minBudget=100&maxBudget=500&status=OPEN&sortBy=maxBudget&sortDirection=DESC&page=0&size=10
```

Para mostrar las más antiguas primero, usa `sortBy=createdAt&sortDirection=ASC`.
Sin parámetros de ordenamiento, se muestran las más recientes primero.
Los empates de presupuesto se ordenan por fecha descendente y los empates de fecha
por ID, para mantener un orden estable entre páginas.

Los rangos invertidos, presupuestos negativos, campos de ordenamiento no permitidos
y formatos inválidos de fecha o enum devuelven HTTP `400`.
