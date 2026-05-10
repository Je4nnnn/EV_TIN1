# Git, versionamiento y GitHub Actions

Esta guia resume la ultima parte del proyecto: estrategia de ramas, versionamiento semantico, CI con GitHub Actions y publicacion de imagenes en DockerHub.

## Decision tomada

La opcion conveniente para este proyecto es usar una variante simple de GitFlow:

- `main`: version estable y demostrable.
- `develop`: integracion de cambios antes de pasar a estable.
- `feature/<nombre>`: ramas cortas para nuevas funcionalidades.
- `release/<version>`: preparacion final antes de publicar una version.
- `hotfix/<nombre>`: correcciones urgentes desde `main`.

No se reemplaza Jenkins. Jenkins queda como automatizacion local del curso y GitHub Actions queda como automatizacion del repositorio remoto. Esta combinacion es mas avanzada porque demuestra dos herramientas de CI/CD y permite publicar imagenes Docker desde GitHub sin depender del computador local.

## Versionamiento semantico

Se usa el formato:

```text
MAJOR.MINOR.PATCH
```

Ejemplos:

- `v1.0.0`: primera version estable para defensa.
- `v1.1.0`: agrega funcionalidad sin romper compatibilidad.
- `v1.1.1`: corrige errores sin agregar funcionalidad grande.

Las imagenes Docker se publican cuando se sube un tag SemVer:

```bash
git tag v1.0.0
git push origin v1.0.0
```

Ese tag genera imagenes:

```text
<DOCKERHUB_USERNAME>/travelagency-backend:1.0.0
<DOCKERHUB_USERNAME>/travelagency-backend:1.0
<DOCKERHUB_USERNAME>/travelagency-backend:latest
<DOCKERHUB_USERNAME>/travelagency-frontend:1.0.0
<DOCKERHUB_USERNAME>/travelagency-frontend:1.0
<DOCKERHUB_USERNAME>/travelagency-frontend:latest
```

## Workflows agregados

### `.github/workflows/ci.yml`

Se ejecuta en `push`, `pull_request` y manualmente. Hace:

- checkout del repositorio
- instala Java 21
- ejecuta `./mvnw -B clean verify`
- instala Node 20
- ejecuta `npm ci`
- ejecuta `npm run lint`
- ejecuta `npm run build`

Este workflow valida que backend y frontend compilen antes de mezclar cambios.

### `.github/workflows/docker-publish.yml`

Se ejecuta al crear tags `v*.*.*` o manualmente. Hace:

- login en DockerHub con secrets
- genera tags Docker desde el tag Git
- construye imagen backend
- construye imagen frontend
- publica ambas imagenes en DockerHub

## Secrets necesarios en GitHub

En el repositorio de GitHub:

1. Entrar a `Settings`.
2. Ir a `Secrets and variables`.
3. Entrar a `Actions`.
4. Abrir la pestana `Secrets`.
5. Crear `New repository secret`.

Crear estos secrets:

```text
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
```

`DOCKERHUB_USERNAME` debe ser tu usuario real de DockerHub. En tu Jenkins aparece `je4nn`; usa ese si ese es tu namespace. El valor `mtisw` solo corresponde si el profesor entrega esa cuenta.

`DOCKERHUB_TOKEN` debe ser un token de DockerHub con permiso `Read & Write`.

En este proyecto ambos se crearon como `Repository secrets`, no como `Environment secrets`, porque los workflows consumen directamente:

```text
secrets.DOCKERHUB_USERNAME
secrets.DOCKERHUB_TOKEN
```

Para el token se recomienda usar un Personal Access Token dedicado a GitHub Actions, por ejemplo `github-actions-hotelrm`, con permiso `Read & Write`.

## Variables recomendadas para el frontend

Como Vite deja las variables dentro del build estatico, si quieres que GitHub Actions publique una imagen frontend lista para tu EC2 actual, crea tambien estas variables en GitHub:

Ruta:

```text
Settings > Secrets and variables > Actions > Variables
```

Variables:

```text
VITE_API_BASE_URL=/
VITE_PAYROLL_BACKEND_SERVER=/
VITE_KEYCLOAK_ENABLED=true
VITE_KEYCLOAK_URL=http://100.53.75.196:8080
VITE_KEYCLOAK_REALM=hotelrm
VITE_KEYCLOAK_CLIENT_ID=hotelrm-frontend
VITE_KEYCLOAK_ADMIN_ROLE=hotelrm_admin
```

Si la IP publica de EC2 cambia, hay que actualizar `VITE_KEYCLOAK_URL` antes de publicar una nueva imagen frontend.

Estas se crearon como `Repository variables`, no como `Environment variables`, porque el proyecto no define ambientes separados como `staging` o `production`.

## Paso a paso recomendado

### 1. Crear rama `develop`

```bash
git checkout main
git pull origin main
git checkout -b develop
git push -u origin develop
```

### 2. Crear rama de trabajo

```bash
git checkout -b feature/github-actions-ci
```

### 3. Subir cambios

```bash
git add .github/workflows docs/GIT_GITHUB_ACTIONS.md docker-compose.yml keycloak/hotelrm-realm.json Frontend/package.json Frontend/package-lock.json GUIA_DEFENSA_PROYECTO.md
git commit -m "ci: add github actions docker publish workflow"
git push -u origin feature/github-actions-ci
```

### 4. Crear Pull Request

Crear PR desde:

```text
feature/github-actions-ci -> develop
```

Revisar que el workflow `CI` quede en verde.

### 5. Preparar release

```bash
git checkout develop
git pull origin develop
git checkout -b release/1.0.0
git push -u origin release/1.0.0
```

Crear PR:

```text
release/1.0.0 -> main
```

### 6. Publicar version

Cuando `main` ya tenga los cambios:

```bash
git checkout main
git pull origin main
git tag v1.0.0
git push origin v1.0.0
```

Esto dispara `Docker Publish` y sube las imagenes a DockerHub.

### 7. Usar imagenes publicadas en EC2

En la instancia:

```bash
cd ~/EV_TIN1
docker compose down
docker compose pull
docker compose up -d
```

Si quieres forzar una version concreta:

```bash
IMAGE_TAG=1.0.0 DOCKERHUB_NAMESPACE=<DOCKERHUB_USERNAME> docker compose up -d
```

## Como explicarlo al profesor

> “Use una estrategia basada en GitFlow: `main` como rama estable, `develop` para integracion, ramas `feature` para cambios, `release` para preparar entregas y tags SemVer para versiones. Agregue GitHub Actions como CI/CD remoto: un workflow valida backend y frontend en cada push o PR, y otro publica imagenes Docker en DockerHub al crear un tag `v1.0.0`. Los secretos de DockerHub se guardan en GitHub Actions, por lo que no quedan credenciales dentro del codigo.”
