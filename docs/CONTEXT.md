# CONTEXTO DEL PROYECTO: VNCU (Vincu)

## 1. Resumen del Proyecto

Desarrollo de un MVP tecnológico ("Vincu") que conecta clientes que necesitan servicios para el hogar con técnicos
independientes ("Maestros Obreros").

- **Objetivo/Público:** Presentación universitaria (evaluado por profesores). MVP funcional en 4 sprints de 3 semanas
  (12 semanas total).
- **Modelo de Negocio (MVP):** Clientes publican solicitudes pagando una tarifa -> Técnicos ven lista por categoría e
  indican interés -> Cliente acepta a uno -> Se revela información de contacto y dirección exacta.

## 2. Stack Tecnológico

- **Frontend:** Next.js (App Router, mobile-first responsivo), Tailwind CSS v4, `react-hook-form` para validaciones,
  Axios centralizado (`src/lib/axios.ts`).
- **Backend:** Spring Boot (Java 21), Arquitectura modular (Controller, Service, Repository), DTOs basados en `records`
  de Java, Spring Security (BCryptPasswordEncoder).
- **Base de Datos:** PostgreSQL (Puerto 5433 local). Uso de `UUID` nativo para llaves primarias (extensión `pgcrypto`).
- **Infraestructura:** Despliegue de manera local por el momento.

## 3. Estado Actual del Desarrollo

### Frontend (`../src`)

- Estructura de carpetas corregida: `app`, `components`, `lib`, `services`, y `types` consolidadas dentro de `../src`.
- **Registro (`/register`):** Formulario dinámico por rol (Cliente/Técnico) validado con `react-hook-form`. Mapeo
  estricto del campo `specialty` con los Enums del backend. Redirección automática tras registro.
- **Login (`/login`):** Formulario funcional. Guarda un token temporal (`vncu_token`) y el rol (`vncu_role`) en
  `localStorage`. Redirige a `/dashboard/client` o `/dashboard/technician`.

### Backend & Base de Datos

- **Seguridad (Auth):** CORS configurado para permitir `http://localhost:3000`. Autenticación actual maneja validación
  manual con `BCryptPasswordEncoder` y devuelve un token temporal simulado ("token-temporal-mvp-123456"). Preparado
  arquitectónicamente para migrar a JWT (Access/Refresh Token) post-MVP.
- **Tablas Base (Completadas):** `document_type`, `users`, `client_profiles`, `technician_profiles`. Existen constraints
  `UNIQUE` en documentos para evitar duplicados.
- **Tablas Geográficas (Completadas):** `ubigeo_departments`, `ubigeo_provinces`, `ubigeo_districts` basadas en el
  estándar del INEI.
- **Data Inicial (Seed):** Insertados los datos geográficos para Lima (15) y Callao (07) con sus respectivas provincias
  y distritos para el funcionamiento del MVP.

## 4. Diseño y Reglas de la Entidad `job_offers` (Publicaciones)

La tabla `job_offers` centraliza el flujo de negocio con las siguientes reglas:

- **Estructura Híbrida de Ubicación:** Almacena `ubigeo_district_id` (para agrupamiento y reportes), `latitude` y
  `longitude` (para búsquedas por radio en kilómetros con la fórmula de Haversine) y `specific_address` (texto plano
  para la calle/número/lote).
- **Regla de Privacidad (Ocultación de Dirección):** El DTO del backend expone las coordenadas al estar el estado en
  `OPEN`, pero envía `specific_address` como `null`. Solo cuando el estado cambia a `IN_PROGRESS` y el usuario que
  consulta es el `technician_id` asignado, se revela la dirección exacta.
- **Relaciones Estrictas:** `client_id` y `technician_id` referencian directamente a las tablas de perfiles
  (`client_profiles` y `technician_profiles`), no a la tabla `users` genérica, evitando asignaciones de roles cruzados.
- **Moneda:** Presupuesto (`max_budget`) almacenado como `DECIMAL(10, 2)`. El símbolo de moneda (S/.) se maneja de forma
  visual y exclusiva en la capa frontend.

## 5. Próximos Pasos (Historias de Usuario Agrupadas)

- **VNCU-19 a VNCU-22 (Creación de Oferta):**
    - Crear Entidad `JobOffer` y DTOs en Spring Boot.
    - Desarrollar Endpoint POST `/api/v1/jobs`.
    - Crear formulario en el panel del cliente capturando lat/lng desde el navegador.
- **VNCU-23 y VNCU-24 (Listado y Detalle):** Endpoints GET para ofertas del cliente y detalles específicos.
- **VNCU-9 (Filtro por Radio/Especialidad):** Query nativa en el repositorio para buscar trabajos usando lat/lng del
  técnico limitando el radio.
- **VNCU-38:** Implementación del menú header responsivo según rol.
- **VNCU-11:** Pantalla y lógica para actualizar perfiles de usuario.