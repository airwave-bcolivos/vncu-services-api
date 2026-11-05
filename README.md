# Vincu - Plataforma de Servicios para el Hogar

Vincu es un MVP (Producto Mínimo Viable) diseñado para conectar clientes que necesitan servicios domésticos con técnicos
independientes ("Maestros Obreros"). Este proyecto académico facilita la publicación de requerimientos, la evaluación de
oficios y la asignación segura de trabajos.

## Tecnologías Utilizadas

* **Frontend:** Next.js (App Router), Tailwind CSS v4, React Hook Form, Axios.
* **Backend:** Spring Boot (Java 21), Spring Security (BCryptPasswordEncoder).
* **Base de Datos:** PostgreSQL (Puerto 5433).
* **Infraestructura:** Despliegue local.

## Características Principales (MVP)

* **Gestión de Roles:** Registro y autenticación segregados para Clientes y Técnicos, con validaciones estrictas para
  evitar documentos de identidad duplicados.
* **Sistema de Publicaciones (`job_offers`):** Los clientes pueden crear ofertas detallando el problema, la especialidad
  requerida (Gasfitería, Electricidad, etc.) y un presupuesto máximo.
* **Geolocalización Híbrida:** Clasificación de direcciones utilizando el estándar de Ubigeo (INEI) para Lima y Callao,
  combinado con coordenadas (latitud y longitud) para aplicar filtros de búsqueda por radio utilizando la fórmula de
  Haversine.
* **Privacidad por Diseño:** Las direcciones específicas de los clientes se ocultan en el estado inicial de la oferta y
  solo se exponen al técnico una vez que la solicitud pasa a estado `IN_PROGRESS`.

## Requisitos Previos

* Node.js (v18 o superior)
* Java Development Kit (JDK 21)
* PostgreSQL (v14 o superior)

## Instalación y Configuración

### 1. Base de Datos

* Crear una base de datos en PostgreSQL.
* Ejecutar los scripts SQL proporcionados para generar el catálogo de `document_type`, las tablas de usuarios y
  perfiles, las tablas de Ubigeo y la tabla principal `job_offers`. Asegurarse de tener habilitada la extensión
  `pgcrypto` para el manejo de UUIDs.

### 2. Backend (Spring Boot)

1. Navegar al directorio del backend.
2. Configurar las credenciales de base de datos en `application.properties` o `application.yml`.
3. Ejecutar el proyecto:
   ```bash
   ./mvnw spring-boot:run