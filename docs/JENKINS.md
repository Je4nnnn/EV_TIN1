# Jenkins paso a paso

Esta guia deja el proyecto listo para CI local en Windows o Linux usando el `Jenkinsfile` de la raiz.

## 1. Instalacion base

Debes tener instalado manualmente:

- Jenkins
- Git
- Docker Desktop
- Node.js

En Windows puedes correr Jenkins como servicio o levantarlo manualmente. Si usas servicio, verifica que el usuario del servicio tenga permiso para usar Docker.

## 2. Verificar herramientas antes de crear el job

En la maquina donde correra Jenkins valida:

```powershell
docker --version
node --version
npm --version
git --version
```

No es obligatorio tener Java 21 preinstalado para el pipeline, porque el repo incluye:

- `scripts/setup-java21.sh`
- `scripts/setup-java21.ps1`

El pipeline descarga JDK 21 en `tools/jdk-21`.

## 3. Plugins recomendados en Jenkins

Instala al menos:

- Pipeline
- Git
- JUnit
- Credentials Binding

Opcionales pero utiles:

- Workspace Cleanup
- Docker Pipeline
- Blue Ocean

## 4. Crear credenciales para Docker Hub

Solo si piensas publicar imagenes.

1. En Jenkins entra a `Manage Jenkins > Credentials`.
2. Crea una credencial tipo `Username with password`.
3. Usa como `ID` algo simple, por ejemplo: `dockerhub`.
4. Guarda tu usuario y password/token de Docker Hub.

Ese ID debe coincidir con el parametro:

```text
DOCKERHUB_CREDENTIALS_ID
```

## 5. Crear el job pipeline

1. `New Item`
2. Nombre sugerido: `hotelrm-ci`
3. Tipo: `Pipeline`
4. `Pipeline definition`: `Pipeline script from SCM`
5. SCM: `Git`
6. URL del repositorio: tu repo actual
7. `Script Path`: `Jenkinsfile`
8. Guardar

## 6. Parametros del pipeline

El `Jenkinsfile` soporta:

- `BUILD_DOCKER_IMAGES`
- `PUSH_DOCKER_IMAGES`
- `DOCKERHUB_NAMESPACE`
- `DOCKERHUB_CREDENTIALS_ID`

Uso recomendado:

- Para validar codigo: `BUILD_DOCKER_IMAGES=false`, `PUSH_DOCKER_IMAGES=false`
- Para build completo local: `BUILD_DOCKER_IMAGES=true`, `PUSH_DOCKER_IMAGES=false`
- Para publicar a Docker Hub: `BUILD_DOCKER_IMAGES=true`, `PUSH_DOCKER_IMAGES=true`

## 7. Que hace el pipeline

### Stage 1. Checkout

Descarga el repo.

### Stage 2. Prepare Java 21

- En Linux usa `scripts/setup-java21.sh`
- En Windows usa `scripts/setup-java21.ps1`

### Stage 3. Verify Tooling

Valida:

- `java --version`
- `node --version`
- `npm --version`
- `docker --version` si vas a construir/publicar imagenes

### Stage 4. Build Backend

Ejecuta:

- Linux: `./mvnw -B clean verify`
- Windows: `mvnw.cmd -B clean verify`

Genera:

- pruebas unitarias
- JAR
- coverage JaCoCo

### Stage 5. Build Frontend

Ejecuta:

- `npm ci`
- `npm run build`

Genera:

- `Frontend/dist`

### Stage 6. Build Docker Images

Si activas build Docker:

- construye `hotelrm-backend`
- construye `hotelrm-frontend`

### Stage 7. Validate Compose Files

Valida sintaxis de:

- `docker-compose.yml`
- `docker/compose-ha.yml`

### Stage 8. Push Docker Images

Si activas push:

- hace `docker login`
- publica backend y frontend
- hace `docker logout`

## 8. Artefactos que Jenkins archivara

- `Backend/target/*.jar`
- `Backend/target/site/jacoco/**/*`
- `Frontend/dist/**`

## 9. Problemas comunes

### Docker no responde desde Jenkins

Causa frecuente:

- el usuario que corre Jenkins no tiene acceso a Docker Desktop

Solucion:

- correr Jenkins con un usuario que tenga acceso a Docker
- o cambiar el servicio de Jenkins para usar tu usuario local

### El frontend falla con `vite` no encontrado

Causa:

- no se ejecutaron dependencias

Solucion:

- el pipeline ya usa `npm ci`; si falla, revisa conectividad o `package-lock.json`

### El backend falla por base de datos

Las pruebas de repositorio usan H2 en memoria, asi que no deberian depender de PostgreSQL.

La aplicacion en runtime si requiere PostgreSQL real.

### Push a Docker Hub falla

Revisa:

- `DOCKERHUB_NAMESPACE`
- `DOCKERHUB_CREDENTIALS_ID`
- credenciales correctas en Jenkins

## 10. Ejecucion recomendada para demo

Primera corrida:

- `BUILD_DOCKER_IMAGES=false`
- `PUSH_DOCKER_IMAGES=false`

Segunda corrida:

- `BUILD_DOCKER_IMAGES=true`
- `PUSH_DOCKER_IMAGES=false`

Corrida de publicacion:

- `BUILD_DOCKER_IMAGES=true`
- `PUSH_DOCKER_IMAGES=true`

## 11. Pasos manuales fuera de Jenkins

Debes hacerlos tu:

1. Configurar PostgreSQL.
2. Configurar Keycloak.
3. Crear usuarios y roles en Keycloak.
4. Crear credenciales Docker Hub en Jenkins.
5. Verificar que Docker Desktop este levantado antes de correr stages Docker.
