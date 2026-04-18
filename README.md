# HotelRM

Proyecto full stack con backend Spring Boot, frontend React/Vite, autenticacion con Keycloak, PostgreSQL local, Docker y pipeline Jenkins.

## Arquitectura operativa

- Keycloak local: `http://localhost:8080`
- Jenkins local: `http://localhost:8081`
- Backend Spring Boot: `http://localhost:8091`
- PostgreSQL local: `localhost:5432`
- Frontend local con Vite: `http://localhost:5173`
- Frontend Docker/Nginx: `http://localhost:3000`

El backend usa PostgreSQL local por defecto y escucha en `8091`. Docker Compose no levanta Keycloak, Jenkins ni PostgreSQL por defecto para evitar choques con los servicios que ya tienes instalados localmente.

## Backend

- Java objetivo: `21`
- Build: Maven Wrapper (`Backend/mvnw`)
- Seguridad: Spring Security OAuth2 Resource Server con JWT de Keycloak
- Perfil por defecto: `local`

### Endpoints publicos

- `GET /api/v1/rooms/**`
- `GET /api/v1/tourist-packages/**`
- `POST /api/v1/reservations`
- `POST /api/v1/reservations/confirmar`
- `GET /api/v1/users/findByRut/**`
- `POST /api/v1/users`
- `PUT /api/v1/users/{id}`
- `GET /actuator/health`

### Endpoints protegidos

Las vistas y operaciones administrativas requieren el rol Keycloak `hotelrm_admin`.

- Rack y cancelacion de reservas
- Reportes operativos
- ABM de paquetes turisticos
- Operaciones administrativas de usuarios y habitaciones

### Endpoint util para validar autenticacion

- `GET /api/v1/auth/me`

## Keycloak

Configura un realm con estos valores:

- Realm: `hotelrm`
- Client ID: `hotelrm-frontend`
- Tipo de cliente recomendado: `Public`
- Standard Flow: `ON`
- PKCE S256: `Required` o `Supported`
- Client secret: no aplica para este SPA

### Redirect URIs sugeridas

- `http://localhost:5173/*`
- `http://localhost:3000/*`

### Web Origins sugeridos

- `http://localhost:5173`
- `http://localhost:3000`

### Rol administrativo

- Realm role: `hotelrm_admin`

Asigna `hotelrm_admin` a los usuarios que deban ver Rack, Reportes y Paquetes turisticos.

### Import rapido del realm

El repo incluye un realm listo para importar en [keycloak/hotelrm-realm.json](/home/sidwilson0/Escritorio/EV_TIN1/keycloak/hotelrm-realm.json) y un script para cargarlo usando `kcadm.sh`.

Si tu Keycloak local esta instalado en `~/Descargas/keycloak-26.6.1` y tu admin es `admin/admin`, ejecuta:

```bash
KEYCLOAK_ADMIN=admin \
KEYCLOAK_ADMIN_PASSWORD=admin \
./scripts/import-keycloak-realm.sh
```

Si tu usuario admin o la ruta de Keycloak son otros, ajusta:

```bash
KEYCLOAK_HOME=~/Descargas/keycloak-26.6.1 \
KEYCLOAK_ADMIN=<tu_admin> \
KEYCLOAK_ADMIN_PASSWORD=<tu_password> \
./scripts/import-keycloak-realm.sh
```

Despues entra al admin console, crea o edita un usuario y asignale el realm role `hotelrm_admin` si necesitas acceso administrativo en la app.

Tambien puedes crear un usuario local de desarrollo con:

```bash
KEYCLOAK_ADMIN=admin \
KEYCLOAK_ADMIN_PASSWORD=admin \
./scripts/create-keycloak-dev-user.sh
```

Por defecto crea:

- username: `hotelrm-admin`
- password: `changeit`
- realm role: `hotelrm_admin`

## Variables de entorno

Usa `.env.example` en la raiz y `Frontend/.env.example` como referencia. Las principales son:

- `SERVER_PORT=8091`
- `DB_HOST=localhost`
- `DB_PORT=5432`
- `DB_NAME=hotelrm`
- `DB_USER=hotelrm_app`
- `DB_PASSWORD=changeit`
- `KEYCLOAK_AUTH_SERVER_URL=http://localhost:8080`
- `KEYCLOAK_REALM=hotelrm`
- `KEYCLOAK_CLIENT_ID=hotelrm-frontend`
- `KEYCLOAK_ADMIN_ROLE=hotelrm_admin`
- `KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/hotelrm`
- `CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000,http://localhost:4173`

Frontend:

- `VITE_API_BASE_URL=http://localhost:8091`
- `VITE_KEYCLOAK_ENABLED=true`
- `VITE_KEYCLOAK_URL=http://localhost:8080`
- `VITE_KEYCLOAK_REALM=hotelrm`
- `VITE_KEYCLOAK_CLIENT_ID=hotelrm-frontend`
- `VITE_KEYCLOAK_ADMIN_ROLE=hotelrm_admin`

## Ejecucion local

### 1. Preparar Java 21 JDK del proyecto

El sistema actual tiene Java 21 runtime, pero no un JDK completo. Este repo incluye un script para dejar Maven operando con Java 21 sin cambiar tu instalacion global:

```bash
./scripts/setup-java21.sh
```

Si trabajas en IntelliJ, apunta el SDK del proyecto a:

```text
<repo>/tools/jdk-21
```

### 2. Levantar backend

```bash
cd Backend
./mvnw spring-boot:run
```

### 3. Levantar frontend

Se recomienda Node `20` para desarrollo. El archivo `Frontend/.nvmrc` ya lo deja declarado.

```bash
cd Frontend
npm ci
npm run dev
```

## Ejecucion con Docker

El compose principal esta en la raiz del repo y solo levanta backend + frontend. Keycloak, Jenkins y PostgreSQL siguen usando tus servicios locales actuales.

```bash
docker compose up --build
```

Esto deja:

- Backend en `http://localhost:8091`
- Frontend en `http://localhost:3000`

Desde los contenedores, el backend usa `host.docker.internal` para llegar al PostgreSQL y Keycloak que corren en tu Ubuntu.

## Jenkins

El pipeline principal esta en `Jenkinsfile` en la raiz.

### Que hace

- descarga/prepara JDK 21 dentro del workspace
- compila y testea backend con Maven
- compila frontend con npm
- archiva el `.jar` y el `dist/`
- opcionalmente construye imagenes Docker con el parametro `BUILD_DOCKER_IMAGES=true`

### Plugins/herramientas necesarias

- Pipeline
- Git
- JUnit
- Docker CLI accesible para el usuario `jenkins` si quieres construir imagenes

### Configuracion sugerida del job

- Tipo: `Pipeline`
- SCM: tu repositorio actual
- Script Path: `Jenkinsfile`

## Verificaciones rapidas

Backend:

```bash
cd Backend
./mvnw test
```

Frontend:

```bash
cd Frontend
npm run build
```
