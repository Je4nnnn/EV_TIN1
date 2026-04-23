# HotelRM

Proyecto full stack con backend Spring Boot, frontend React/Vite, autenticacion con Keycloak, PostgreSQL, Docker, Docker Compose, pruebas unitarias y pipeline Jenkins.

## Stack principal

- Backend: Spring Boot, Spring Data JPA, Spring Security, OAuth2 Resource Server, Lombok
- Base de datos: PostgreSQL
- Documentacion API: Springdoc OpenAPI + Swagger UI
- Frontend: React + Vite + Material UI + Axios + React Router
- Seguridad: Keycloak + JWT
- Infraestructura: Docker, Nginx, Docker Compose
- CI: Jenkins

## URLs esperadas

### Desarrollo local

- Frontend Vite: `http://localhost:5173/home`
- Backend Spring Boot: `http://localhost:8090`
- Swagger UI: `http://localhost:8090/swagger-ui/index.html`
- Keycloak: `http://localhost:8080`

### Docker simple

- Frontend: `http://localhost:8070`
- Backend: `http://localhost:8090`

### Docker HA

- Frontend balanceado: `http://localhost:8070`
- Backend balanceado: `http://localhost:8090`

## Arquitectura por capas

- `Controllers`: reciben peticiones HTTP y exponen endpoints REST.
- `Services`: implementan reglas de negocio.
- `Repositories`: acceso a persistencia con Spring Data JPA.
- `Entities`: modelo persistente.
- `Config`: seguridad, OpenAPI, inicializacion y propiedades.
- `DTOs`: contratos de respuesta auxiliares.

## Backend

### Requisitos

- JDK 21
- PostgreSQL corriendo
- Keycloak configurado

### Ejecutar localmente

```bash
cd Backend
./mvnw spring-boot:run
```

En Windows:

```powershell
cd Backend
.\mvnw.cmd spring-boot:run
```

### Build para produccion

```bash
cd Backend
./mvnw clean package -DskipTests
```

El artefacto queda en:

```text
Backend/target/hotelrm-backend.jar
```

### Ejecutar el JAR

```bash
java -DDB_HOST=localhost -jar Backend/target/hotelrm-backend.jar
```

### Swagger

- URL: `http://localhost:8090/swagger-ui/index.html`

## Frontend

### Requisitos

- Node.js 20 o superior

### Ejecutar localmente

```bash
cd Frontend
npm ci
npm run dev
```

### Build para produccion

```bash
cd Frontend
npm ci
npm run build
```

El build queda en:

```text
Frontend/dist
```

## Seguridad con Keycloak

### Variables relevantes

- `KEYCLOAK_AUTH_SERVER_URL=http://localhost:8080`
- `KEYCLOAK_REALM=hotelrm`
- `KEYCLOAK_CLIENT_ID=hotelrm-frontend`
- `KEYCLOAK_ADMIN_ROLE=hotelrm_admin`
- `KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/hotelrm`

### Comportamiento

- Endpoints publicos siguen abiertos para flujo de reserva.
- Endpoints administrativos quedan protegidos con `@PreAuthorize`.
- El frontend protege vistas administrativas mediante `RequireRole`.
- Axios agrega el token JWT automaticamente cuando el usuario inicia sesion.

## Pruebas

### Backend

```bash
cd Backend
./mvnw clean verify
```

Se ejecutan:

- pruebas de servicios
- pruebas de repositorios
- pruebas de controladores
- reporte de cobertura JaCoCo

Reporte de coverage:

```text
Backend/target/site/jacoco/index.html
```

## Docker

### Backend

```bash
docker build -t hotelrm-backend:latest Backend
docker run -e DB_HOST=host.docker.internal -p 8090:8090 hotelrm-backend:latest
```

### Frontend

```bash
docker build -t hotelrm-frontend:latest Frontend
docker run -p 8070:80 hotelrm-frontend:latest
```

## Docker Compose

### Despliegue simple

```bash
docker compose up --build
```

### Despliegue HA

```bash
docker compose -f docker/compose-ha.yml up --build
```

Archivos clave:

- `docker-compose.yml`: frontend + backend simples
- `docker/compose-ha.yml`: 3 frontends + 3 backends + 2 balanceadores
- `docker/nginx-frontend.conf`: balanceador del frontend
- `docker/nginx-backend.conf`: balanceador del backend

## Jenkins

El pipeline principal esta en:

```text
Jenkinsfile
```

Incluye:

- descarga de JDK 21 para Linux y Windows
- build y test del backend
- build del frontend
- archivado de JAR, reportes y `dist/`
- build opcional de imagenes Docker
- push opcional a Docker Hub
- validacion de `docker compose`

Guia paso a paso:

- ver [docs/JENKINS.md](docs/JENKINS.md)

## Pasos manuales que debes hacer tu

- levantar PostgreSQL con una base compatible con la app
- configurar Keycloak con realm, clientes, roles y usuarios
- crear credenciales de Docker Hub en Jenkins si vas a publicar imagenes
- instalar y levantar Jenkins
- dar acceso a Docker al usuario que corre Jenkins

## Archivos clave del proyecto

- `Backend/pom.xml`
- `Backend/src/main/resources/application.properties`
- `Backend/src/main/java/kartingRM/Backend/Config/SecurityConfig.java`
- `Backend/src/main/java/kartingRM/Backend/Config/OpenApiConfig.java`
- `Frontend/src/App.jsx`
- `Frontend/src/auth/AuthContext.jsx`
- `Frontend/src/services/api.js`
- `docker-compose.yml`
- `docker/compose-ha.yml`
- `Jenkinsfile`
