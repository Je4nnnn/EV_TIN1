# Guia de Defensa Proyecto HotelRM

## 1. Resumen ejecutivo

`HotelRM` es una aplicacion web de gestion hotelera desarrollada con:

- `Frontend`: React + Vite + Material UI
- `Backend`: Spring Boot + JPA + Maven
- `Base de datos`: PostgreSQL
- `Autenticacion`: Keycloak + JWT
- `Despliegue`: Docker + Docker Compose
- `Integracion continua`: Jenkins
- `Pruebas`: JUnit + Mockito + JaCoCo

La idea general es:

1. el usuario entra al frontend
2. el frontend consume el backend por HTTP
3. si una ruta requiere permisos, el usuario inicia sesion en Keycloak
4. Keycloak entrega un `JWT`
5. el frontend envia ese token al backend
6. el backend valida el token y autoriza segun rol
7. el backend consulta o actualiza PostgreSQL

---

## 2. Que hace la aplicacion

La aplicacion permite gestionar operaciones hoteleras como:

- ver habitaciones y su disponibilidad
- consultar precios
- registrar reservas
- administrar habitaciones
- administrar paquetes turisticos
- ver rack operativo
- ver reportes

Las vistas publicas pueden ser usadas sin iniciar sesion.  
Las vistas administrativas requieren un usuario con rol `hotelrm_admin`.

---

## 3. Como esta organizada la arquitectura

El backend sigue `Layered Architecture`, o arquitectura por capas.

### Capas del backend

#### `Controller`

Recibe peticiones HTTP del frontend o de Swagger/Postman.

Ejemplos:

- `RoomController`
- `ReservationController`
- `TouristPackageController`
- `UserController`
- `AuthController`

Responsabilidad:

- exponer endpoints REST
- recibir parametros
- devolver respuestas HTTP

#### `Service`

Es la capa de logica de negocio.

Ejemplos:

- `RoomService`
- `ReservationService`
- `TouristPackageService`
- `UserService`

Responsabilidad:

- validar reglas del negocio
- calcular disponibilidad
- aplicar descuentos
- controlar estados
- coordinar repositorios

#### `Repository`

Es la capa de acceso a datos usando Spring Data JPA.

Ejemplos:

- `RoomRepository`
- `ReservationRepository`
- `TouristPackageRepository`
- `UserRepository`

Responsabilidad:

- consultar la base de datos
- guardar entidades
- buscar por ID o por campos derivados

#### `Entity`

Representa tablas de la base de datos.

Ejemplos:

- `RoomEntity`
- `ReservationEntity`
- `ReservationDetailsEntity`
- `TouristPackageEntity`
- `UserEntity`

Responsabilidad:

- modelar los datos persistidos

### Explicacion corta para el profesor

Si pregunta por la arquitectura:

> “Use una arquitectura por capas. El controller recibe HTTP, el service contiene la logica del negocio, el repository accede a la base de datos y la entity representa las tablas.”

---

## 4. Tecnologias y para que sirve cada una

### Spring Boot

Sirve para construir el backend rapidamente.

Ventajas:

- autoconfiguracion
- servidor embebido
- integracion facil con JPA, seguridad y testing

### Maven

Sirve para:

- manejar dependencias
- compilar
- correr tests
- generar el `.jar`

Archivo clave:

- `Backend/pom.xml`

### PostgreSQL

Es la base de datos relacional del proyecto.

Si el profesor pregunta por que no MySQL:

> “En clases se uso MySQL como ejemplo, pero se dio libertad. Mantengo la misma idea relacional usando PostgreSQL, que es totalmente compatible con el enfoque JPA.”

### React + Vite

Sirven para el frontend.

- `React` construye la interfaz basada en componentes
- `Vite` acelera el entorno de desarrollo y el build

### Material UI

Sirve para los componentes visuales del frontend.

### Axios

Sirve para consumir el backend desde el frontend.

### Keycloak

Sirve para autenticacion e identidad.

Responsabilidad:

- login
- usuarios
- roles
- emision de tokens JWT

### JWT

Es el token que representa al usuario autenticado.

Incluye informacion como:

- subject
- username
- email
- roles

### Docker

Sirve para empaquetar el frontend y el backend en contenedores.

### Docker Compose

Sirve para levantar varios servicios coordinados.

### Jenkins

Sirve para automatizar:

- build
- tests
- artefactos
- imagenes Docker

### JaCoCo

Sirve para medir cobertura de pruebas.

---

## 5. Estructura real del proyecto

### Carpeta `Backend`

Contiene la API Spring Boot.

Archivos importantes:

- `Backend/src/main/java/.../Controllers`
- `Backend/src/main/java/.../Services`
- `Backend/src/main/java/.../Repositories`
- `Backend/src/main/java/.../Entities`
- `Backend/src/main/resources/application.properties`
- `Backend/pom.xml`

### Carpeta `Frontend`

Contiene la aplicacion React.

Archivos importantes:

- `Frontend/src/App.jsx`
- `Frontend/src/main.jsx`
- `Frontend/src/auth/keycloak.js`
- `Frontend/src/services/api.js`
- `Frontend/src/components/auth/RequireRole.jsx`
- `Frontend/src/views/*`

### Archivos de despliegue

- `docker-compose.yml`
- `Backend/Dockerfile`
- `Frontend/Dockerfile`
- `Jenkinsfile`
- `Frontend/Jenkinsfile`

### Archivos de seguridad

- `keycloak/hotelrm-realm.json`

---

## 6. Flujo completo de funcionamiento

### Flujo publico

1. el usuario entra al frontend
2. navega por `Home`, `Rooms`, `Prices`, `Formulario`
3. el frontend hace llamadas HTTP al backend
4. el backend responde con datos desde PostgreSQL

### Flujo autenticado

1. el usuario intenta entrar a una vista administrativa
2. el componente `RequireRole` detecta que necesita login
3. se redirige a Keycloak
4. el usuario inicia sesion
5. Keycloak devuelve un token JWT
6. `axios` agrega el token en el header `Authorization`
7. el backend valida el JWT con Spring Security
8. si el usuario tiene rol `hotelrm_admin`, se permite el acceso

---

## 7. Como funciona el frontend

### Archivo `App.jsx`

Define las rutas principales de la aplicacion.

Ejemplos:

- `/home`
- `/contact`
- `/rooms`
- `/prices`
- `/formulario`
- `/rack`
- `/reports`
- `/tourist-packages`

Las rutas `rack`, `reports` y `tourist-packages` estan protegidas con `RequireRole`.

### Archivo `api.js`

Configura `axios`.

Responsabilidad:

- definir la URL base del backend
- agregar el token JWT automaticamente si existe
- estandarizar mensajes de error

### Archivo `keycloak.js`

Configura la conexion del frontend con Keycloak.

Responsabilidad:

- leer variables de entorno
- definir realm y client ID
- crear instancia Keycloak
- refrescar token si esta por vencer

### Archivo `RequireRole.jsx`

Controla el acceso a rutas por rol.

Si no hay sesion:

- muestra mensaje
- permite iniciar sesion

Si hay sesion pero no rol:

- muestra mensaje de permisos insuficientes

### Archivo `main.jsx`

Es el punto de entrada del frontend.

Responsabilidad:

- montar React
- inicializar el contexto de autenticacion

### Vistas principales

- `Home`: portada del sistema
- `Rooms`: consulta de habitaciones
- `Prices`: muestra precios
- `Formulario`: reserva
- `Rack`: administracion operativa
- `Reports`: reportes
- `TouristPackages`: gestion de paquetes

---

## 8. Como funciona el backend

### `application.properties`

Centraliza configuracion como:

- puerto del backend
- conexion a PostgreSQL
- seguridad
- CORS
- issuer de Keycloak

### `SecurityConfig`

Configura Spring Security.

Responsabilidad:

- permitir endpoints publicos
- exigir autenticacion en endpoints privados
- validar JWT
- aplicar CORS

### `OpenApiConfig`

Configura Swagger/OpenAPI.

Responsabilidad:

- documentar la API
- permitir probar endpoints desde navegador

### `AuthController`

Devuelve informacion del usuario autenticado.

Endpoint util:

- `GET /api/v1/auth/me`

### `RoomController`

Expone endpoints para habitaciones.

### `ReservationController`

Expone endpoints para reservas y reportes.

### `TouristPackageController`

Expone endpoints para paquetes turisticos.

### `UserController`

Gestiona usuarios del sistema.

### `ReservationService`

Es una de las clases mas importantes del negocio.

Responsabilidad:

- guardar reservas
- validar fechas
- calcular montos
- aplicar reglas de grupo y frecuencia
- construir reportes

### `RoomService`

Responsabilidad:

- obtener disponibilidad
- evitar solapamientos
- administrar inventario base

### `TouristPackageService`

Responsabilidad:

- crear paquetes
- normalizar datos
- controlar cupos y disponibilidad

### `UserService`

Responsabilidad:

- normalizar usuarios
- calcular categoria por visitas
- aplicar descuentos por frecuencia

---

## 9. Seguridad: como funciona Keycloak en este proyecto

### Concepto

Keycloak es el sistema de autenticacion externo.  
La app no maneja usuarios o contraseñas directamente en el frontend ni en el backend.

### Realm usado

- `hotelrm`

### Cliente frontend

- `hotelrm-frontend`

Es un cliente tipo SPA/publico.

### Rol administrativo

- `hotelrm_admin`

### Usuario de demo

- usuario: `hotelrm-admin`
- clave: `changeit`

### Panel admin de Keycloak

- usuario: `admin`
- clave: `admin`

### Si el profesor pregunta “¿por que usaste Keycloak?”

> “Porque centraliza autenticacion, manejo de usuarios, roles y emision de JWT. El frontend solo redirige al login y el backend valida el token.”

### Si pregunta “¿que hace el JWT?”

> “Transporta la identidad y los roles del usuario. El backend lo valida en cada request para autorizar acceso sin mantener sesion en servidor.”

---

## 10. Base de datos y persistencia

### Como se conecta el backend

El backend usa Spring Data JPA con PostgreSQL.

La configuracion vive en:

- `application.properties`

Variables principales:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`

### Que representa cada entidad

- `UserEntity`: cliente o usuario del sistema
- `RoomEntity`: habitacion
- `ReservationEntity`: reserva general
- `ReservationDetailsEntity`: detalle por huesped
- `TouristPackageEntity`: paquete turistico

### Si el profesor pregunta “¿donde cambias la base de datos?”

> “En `application.properties` o por variables de entorno como `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y `DB_PASSWORD`.”

---

## 11. Swagger, Postman y prueba de endpoints

### Swagger

URL:

- `http://localhost:8091/swagger-ui/index.html`

Sirve para:

- ver documentacion de la API
- probar endpoints desde el navegador

### Postman

Tambien se puede usar para:

- probar `GET`, `POST`, `PUT`, `DELETE`
- enviar JSON
- enviar token JWT en headers

Si el profesor pregunta “¿para que sirve Swagger?”

> “Sirve para documentar automaticamente la API y facilitar pruebas sin depender solo del frontend.”

---

## 12. Docker y Docker Compose

### Docker

Cada servicio puede empaquetarse como imagen.

- `Backend/Dockerfile`
- `Frontend/Dockerfile`

### Docker Compose

Archivo:

- `docker-compose.yml`

Responsabilidad:

- levantar backend y frontend coordinados
- inyectar variables de entorno
- mapear puertos

### Puertos relevantes

Entorno local actual:

- frontend Vite: `5173`
- frontend Docker demo: `3001`
- backend: `8091`
- keycloak: `8080`
- jenkins: `8081`

Nota:

El frontend Docker de referencia puede correr en `3000`, pero en este equipo se uso `3001` para evitar conflicto con otro `nginx` local.

### Si el profesor pregunta “¿que hace Docker aqui?”

> “Empaqueta el backend y el frontend con sus dependencias para que se puedan ejecutar de forma consistente sin depender del entorno manual.”

### Si pregunta “¿que hace Compose?”

> “Coordina multiples contenedores y sus variables para levantar el sistema con un solo comando.”

---

## 13. Jenkins

### Archivo principal

- `Jenkinsfile`

### Que hace el pipeline principal

1. hace checkout del codigo
2. prepara Java 21
3. ejecuta `mvn clean verify` en backend
4. ejecuta `npm ci` y `npm run build` en frontend
5. archiva artefactos
6. opcionalmente construye imagenes Docker

### Para que sirve Jenkins

Sirve para automatizar validaciones del proyecto y evitar builds manuales repetitivos.

### Si el profesor pregunta “¿por que agregaste Jenkins?”

> “Para automatizar compilacion, testing y artefactos, que es la idea de integracion continua.”

---

## 14. Pruebas y cobertura

### Tipos de pruebas que hay

- pruebas de `Repository`
- pruebas de `Service`
- pruebas de `Controller`
- pruebas de configuracion y seguridad puntual

### Herramientas usadas

- `JUnit`
- `Mockito`
- `Spring Test`
- `JaCoCo`

### Cobertura actual

Cobertura aproximada actual del backend:

- `Line coverage`: `71.7%`
- `Instruction coverage`: `68.1%`
- `Branch coverage`: `61.2%`
- `Method coverage`: `79.2%`
- `Class coverage`: `100%`

### Si el profesor pregunta “¿que mide JaCoCo?”

> “Mide cuanto del codigo fue ejecutado por los tests, por ejemplo lineas, ramas, metodos y clases.”

### Si pregunta “¿donde ves la cobertura?”

> “En el reporte HTML generado por Maven en `Backend/target/site/jacoco/index.html`.”

---

## 15. Donde editar cada cosa

### Si quiero cambiar rutas del frontend

- `Frontend/src/App.jsx`

### Si quiero cambiar el menu o navbar

- `Frontend/src/components/NavBar/Navbar.jsx`

### Si quiero cambiar la URL del backend en frontend

- `Frontend/src/services/api.js`
- variables `VITE_API_BASE_URL`

### Si quiero cambiar login o Keycloak en frontend

- `Frontend/src/auth/keycloak.js`
- `Frontend/src/auth/AuthContext.jsx`
- `Frontend/src/components/auth/RequireRole.jsx`

### Si quiero cambiar una vista

- `Frontend/src/views/...`

### Si quiero cambiar endpoints del backend

- `Backend/src/main/java/.../Controllers`

### Si quiero cambiar reglas del negocio

- `Backend/src/main/java/.../Services`

### Si quiero cambiar consultas a BD

- `Backend/src/main/java/.../Repositories`

### Si quiero cambiar estructura de datos persistidos

- `Backend/src/main/java/.../Entities`

### Si quiero cambiar seguridad

- `Backend/src/main/java/.../Config/SecurityConfig.java`

### Si quiero cambiar configuracion general

- `Backend/src/main/resources/application.properties`

### Si quiero cambiar Docker

- `Backend/Dockerfile`
- `Frontend/Dockerfile`
- `docker-compose.yml`

### Si quiero cambiar Jenkins

- `Jenkinsfile`
- `Frontend/Jenkinsfile`

---

## 16. Preguntas tipicas del profesor y respuestas cortas

### “¿Por que elegiste esta arquitectura?”

Porque separa responsabilidades, facilita mantenimiento, testing y escalabilidad.

### “¿Que hace un controller?”

Expone endpoints y responde HTTP.

### “¿Que hace un service?”

Aplica la logica del negocio.

### “¿Que hace un repository?”

Accede a la base de datos usando JPA.

### “¿Que es una entity?”

Es la representacion de una tabla en Java.

### “¿Que es Spring Boot?”

Framework para construir aplicaciones Java con configuracion simplificada y servidor embebido.

### “¿Que hace Maven?”

Gestiona dependencias, compila, prueba y empaqueta.

### “¿Que hace React?”

Construye la interfaz en componentes reutilizables.

### “¿Que hace Axios?”

Consume la API backend desde el frontend.

### “¿Que hace Keycloak?”

Gestiona autenticacion, usuarios, roles y tokens JWT.

### “¿Que hace Spring Security?”

Protege endpoints y valida el JWT.

### “¿Que hace Swagger?”

Documenta y permite probar la API.

### “¿Que hace Docker?”

Empaqueta y ejecuta servicios de forma portable.

### “¿Que hace Jenkins?”

Automatiza build, tests y despliegue tecnico.

### “¿Por que PostgreSQL?”

Porque cumple el mismo objetivo relacional del curso y el profesor permitio libertad de herramienta mientras se mantuviera la idea.

---

## 17. Orden sugerido para mostrar el proyecto

### Paso 1. Frontend

Abrir:

- `http://localhost:3001/home`

Mostrar:

- home
- habitaciones
- precios
- formulario

### Paso 2. Seguridad

Entrar a una vista administrativa:

- `Rack`
- `Reports`
- `Tourist Packages`

Mostrar que pide iniciar sesion.

### Paso 3. Login

Usar:

- usuario `hotelrm-admin`
- clave `changeit`

Mostrar que ahora permite acceso.

### Paso 4. Backend

Abrir:

- `http://localhost:8091/swagger-ui/index.html`
- `http://localhost:8091/actuator/health`

### Paso 5. Keycloak

Abrir:

- `http://localhost:8080/admin/`

Explicar:

- realm
- cliente frontend
- rol `hotelrm_admin`

### Paso 6. Explicacion tecnica

Mostrar archivos:

- `App.jsx`
- `api.js`
- `keycloak.js`
- `SecurityConfig.java`
- un `Controller`
- un `Service`
- un `Repository`
- una `Entity`
- `docker-compose.yml`
- `Jenkinsfile`

---

## 18. Comandos utiles para la defensa

### Frontend local

```bash
cd Frontend
npm run dev
```

### Frontend build

```bash
cd Frontend
npm run build
```

### Backend local

```bash
cd Backend
./mvnw spring-boot:run
```

### Backend tests

```bash
cd Backend
./mvnw test
```

### Backend con cobertura

```bash
cd Backend
./mvnw verify
```

### Docker Compose

```bash
docker compose up --build
```

### Si el puerto 3000 esta ocupado

```bash
FRONTEND_PORT_DOCKER=3001 docker compose up --build
```

---

## 19. Despliegue en AWS EC2 realizado para la nube

Para la etapa de nube desplegue el proyecto en una instancia `EC2` de AWS usando Docker Compose. La idea fue mantener la misma arquitectura local, pero ejecutandola en una maquina virtual publica.

### Decision de infraestructura

Use:

- Proveedor: `AWS`
- Servicio: `EC2`
- Region: `us-east-1` / Norte de Virginia
- Sistema operativo: `Amazon Linux 2023`
- Arquitectura: `x86_64`
- Tipo de instancia: `c7i-flex.large`
- Disco: `20 GiB gp3`
- Llave SSH: `hotelrm-key.pem`

Escogi `EC2` porque corresponde a `IaaS`: yo administro la maquina, instalo Docker y decido como levantar los servicios. Para una evaluacion universitaria es una opcion clara porque permite mostrar sistema operativo, red, puertos, contenedores y despliegue completo.

No use `t3.micro` porque el proyecto levanta varios servicios al mismo tiempo:

- PostgreSQL
- Keycloak
- tres replicas del backend Spring Boot
- frontend Nginx
- Nginx frontal

Una instancia con `1 GB RAM` puede quedar corta para Keycloak y Java. Por eso use `c7i-flex.large`, que entrega mas memoria y estabilidad para la demo, manteniendome dentro del contexto de creditos/free tier disponibles en la cuenta.

### Reglas de red usadas

Configure un Security Group con:

- `22/tcp` para SSH
- `80/tcp` para la aplicacion web por HTTP
- `8080/tcp` para Keycloak

El backend tambien publica `8091`, pero la ruta recomendada es entrar por Nginx usando:

```text
http://<IP_PUBLICA>/api/...
```

Para la demo se dejo acceso abierto desde `0.0.0.0/0`, porque el objetivo era evitar bloqueos de red durante la defensa. En un entorno real, SSH deberia restringirse a mi IP y Keycloak deberia ir detras de HTTPS.

### Preparacion de la instancia

Luego de lanzar la instancia me conecte por SSH:

```bash
chmod 400 hotelrm-key.pem
ssh -i hotelrm-key.pem ec2-user@<IP_PUBLICA>
```

Instale las herramientas necesarias:

```bash
sudo dnf update -y
sudo dnf install -y git docker rsync
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user
```

Despues cerre y volvi a abrir la sesion para que el grupo `docker` aplicara.

Instale Docker Compose como plugin:

```bash
mkdir -p ~/.docker/cli-plugins
curl -SL https://github.com/docker/compose/releases/download/v5.1.2/docker-compose-linux-x86_64 -o ~/.docker/cli-plugins/docker-compose
chmod +x ~/.docker/cli-plugins/docker-compose
docker compose version
```

Tambien instale `buildx`, porque `docker compose build` lo requeriria:

```bash
curl -SL https://github.com/docker/buildx/releases/download/v0.17.1/buildx-v0.17.1.linux-amd64 -o ~/.docker/cli-plugins/docker-buildx
chmod +x ~/.docker/cli-plugins/docker-buildx
docker buildx version
```

### Como subi el proyecto

Use `rsync` desde mi computador hacia la EC2 para copiar el proyecto sin subir archivos pesados o temporales:

```bash
rsync -av --delete \
  -e "ssh -i ~/Escritorio/hotelrm-key.pem" \
  --exclude='.git' \
  --exclude='.idea' \
  --exclude='.env' \
  --exclude='Frontend/node_modules' \
  --exclude='Frontend/dist' \
  --exclude='Backend/target' \
  --exclude='tools/jdk-21' \
  --exclude='tools/jdk-21.tar.gz' \
  --exclude='tools/.cache' \
  EV_TIN1/ ec2-user@<IP_PUBLICA>:~/EV_TIN1/
```

Luego configure variables de entorno desde `.env.aws.example`:

```bash
cd ~/EV_TIN1
cp .env.aws.example .env
sed -i 's|<EC2_PUBLIC_IP>|<IP_PUBLICA>|g' .env
```

Las variables importantes fueron:

- `APP_PORT=80`
- `KEYCLOAK_PORT_DOCKER=8080`
- `VITE_KEYCLOAK_PUBLIC_URL=http://<IP_PUBLICA>:8080`
- `KEYCLOAK_ISSUER_URI_DOCKER=http://<IP_PUBLICA>:8080/realms/hotelrm`
- `VITE_API_BASE_URL_DOCKER=/`

Con esto el frontend usa rutas relativas para la API, y Nginx reenvia `/api` al backend.

### Problemas reales encontrados y solucionados

#### 1. Docker Compose requeria Buildx

Al ejecutar:

```bash
docker compose up --build -d
```

aparecio:

```text
compose build requires buildx 0.17.0 or later
```

Solucion: instale `docker-buildx` como plugin de Docker CLI.

#### 2. Keycloak fallo al importar el realm por JSON invalido

Al intentar agregar redirects con `sed`, el archivo `keycloak/hotelrm-realm.json` quedo con comas mal ubicadas y Keycloak fallo con:

```text
Unexpected character ',' expected a value
```

Solucion: limpie las lineas invalidas, valide el JSON y luego agregue los cambios correctamente.

Validacion usada:

```bash
python3 -m json.tool keycloak/hotelrm-realm.json >/dev/null && echo "JSON OK"
```

#### 3. Keycloak exigia HTTPS

En navegador aparecio:

```text
HTTPS required
```

Esto ocurria porque Keycloak, al estar expuesto por IP publica y HTTP, exigia SSL para peticiones externas.

Solucion:

```bash
docker compose exec keycloak /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 \
  --realm master \
  --user admin \
  --password admin

docker compose exec keycloak /opt/keycloak/bin/kcadm.sh update realms/master -s sslRequired=none
docker compose exec keycloak /opt/keycloak/bin/kcadm.sh update realms/hotelrm -s sslRequired=none
```

Tambien deje persistido en `keycloak/hotelrm-realm.json`:

```json
"sslRequired": "none"
```

Esto es aceptable para la demo universitaria, pero en produccion se deberia usar HTTPS real.

#### 4. El boton de login no funcionaba por Web Crypto en HTTP

El frontend cargaba, pero `INICIAR SESION` no funcionaba. En la consola del navegador aparecio:

```text
Web Crypto API is not available
```

La causa fue que `keycloak-js` moderno requiere APIs criptograficas del navegador, y Chrome solo las habilita en contextos seguros (`HTTPS` o `localhost`). Una IP publica con `HTTP` no cumple esa condicion.

Solucion final: usar una version anterior compatible para esta demo:

- contenedor Keycloak: `quay.io/keycloak/keycloak:21.1.2`
- paquete frontend: `keycloak-js: 21.1.2`

Tambien regenere `package-lock.json` porque el Dockerfile del frontend usa `npm ci`.

```bash
cd ~/EV_TIN1/Frontend
docker run --rm -v "$PWD":/app -w /app node:20-alpine npm install --package-lock-only
```

#### 5. Problema con PKCE

Despues del downgrade aparecio:

```text
Missing parameter: code_challenge_method
```

La causa fue una inconsistencia: Keycloak esperaba PKCE, pero el frontend no estaba enviando el metodo correctamente.

Solucion: deje ambos lados consistentes con `S256`.

En frontend:

```js
keycloak.init({
  onLoad: 'check-sso',
  pkceMethod: 'S256',
  checkLoginIframe: false,
  silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
})
```

En Keycloak:

```bash
docker compose exec -T keycloak /opt/keycloak/bin/kcadm.sh get clients/$CLIENT_ID -r hotelrm > /tmp/hotelrm-client.json
```

Luego deje en el cliente:

```json
"attributes": {
  "pkce.code.challenge.method": "S256"
}
```

Con esto el flujo de autenticacion quedo funcionando correctamente.

#### 6. Nginx frontal se detuvo una vez

Despues de reiniciar servicios, el contenedor `travelagency-nginx` quedo detenido porque intento iniciar antes de resolver `backend1`.

Solucion:

```bash
docker compose up -d nginx
```

Luego valide:

```bash
curl -i http://localhost/actuator/health
```

Respuesta esperada:

```json
{"status":"UP"}
```

### Resultado final

Finalmente la aplicacion quedo disponible en:

```text
http://<IP_PUBLICA>/home
```

Keycloak quedo disponible en:

```text
http://<IP_PUBLICA>:8080
```

Credenciales de la app:

```text
hotelrm-admin
changeit
```

Credenciales de administracion de Keycloak:

```text
admin
admin
```

Se valido que:

- el frontend carga desde EC2
- la API responde por Nginx
- `GET /actuator/health` retorna `UP`
- Keycloak permite iniciar sesion
- el usuario `hotelrm-admin` entra con rol `hotelrm_admin`
- se pueden crear, ver, actualizar y eliminar datos
- las rutas administrativas quedan protegidas por rol

### Como explicarlo al profesor

> “Levante una instancia EC2 como IaaS. Instale Docker, Docker Compose y Buildx. Subi el proyecto con rsync y configure variables de entorno para usar la IP publica. El stack corre en contenedores: PostgreSQL, Keycloak, backend replicado, frontend y Nginx frontal. Tuve que resolver problemas reales de nube: puertos, Security Groups, URLs publicas de Keycloak, SSL requerido, Web Crypto en HTTP y compatibilidad de version de Keycloak. Finalmente deje el login funcionando con Keycloak 21.1.2 y PKCE S256, y valide que la aplicacion funciona desde la IP publica de AWS.”

### Como apagar para no gastar creditos

Para detener solo la aplicacion dentro de la instancia:

```bash
cd ~/EV_TIN1
docker compose down
```

Para dejar de gastar por la instancia EC2, se debe detener desde la consola AWS:

1. EC2
2. Instancias
3. seleccionar `hotelrm-ec2`
4. Estado de la instancia
5. Detener instancia

Importante:

- `Detener` conserva el disco, pero la IP publica puede cambiar al volver a iniciar.
- `Terminar` elimina la instancia.
- Si cambia la IP publica, hay que actualizar `.env`, redirects de Keycloak y reconstruir frontend.
- El volumen EBS puede seguir generando costo pequeno aunque la instancia este detenida.

---

## 20. GitFlow, SemVer y GitHub Actions

### Decision tomada

Para la ultima etapa del proyecto agregue una estrategia de ramas basada en GitFlow y automatizacion con GitHub Actions.

La estrategia propuesta es:

- `main`: rama estable, usada para versiones listas para demostrar.
- `develop`: rama de integracion antes de pasar a estable.
- `feature/<nombre>`: ramas para nuevas funcionalidades o ajustes.
- `release/<version>`: preparacion de una version final.
- `hotfix/<nombre>`: correcciones urgentes desde `main`.

Esta opcion es conveniente para el proyecto porque no elimina Jenkins; lo complementa. Jenkins queda como pipeline local visto en clases y GitHub Actions queda como pipeline remoto asociado al repositorio en GitHub.

### Versionamiento semantico

Use versionamiento semantico con tags:

```text
vMAJOR.MINOR.PATCH
```

Ejemplo:

```bash
git tag v1.0.0
git push origin v1.0.0
```

El tag `v1.0.0` representa una version estable del proyecto. Al crear ese tag se activa el workflow de publicacion Docker.

### GitHub Actions agregados

Agregue dos workflows:

```text
.github/workflows/ci.yml
.github/workflows/docker-publish.yml
```

`ci.yml` valida el proyecto en cada `push`, `pull_request` o ejecucion manual:

- instala Java 21
- ejecuta pruebas del backend con Maven
- instala Node 20
- ejecuta lint del frontend
- construye el frontend con Vite

`docker-publish.yml` se ejecuta cuando se crea un tag SemVer como `v1.0.0` o manualmente desde GitHub Actions:

- inicia sesion en DockerHub usando secrets
- construye imagen backend
- construye imagen frontend
- publica las imagenes en DockerHub
- genera tags como `1.0.0`, `1.0` y `latest`

### Secrets necesarios

En GitHub configure:

```text
Settings > Secrets and variables > Actions > Secrets
```

Secrets:

```text
DOCKERHUB_USERNAME
DOCKERHUB_TOKEN
```

`DOCKERHUB_USERNAME` debe ser el usuario real de DockerHub. En mi caso, si uso el mismo namespace que Jenkins, corresponde a `je4nn`. El valor `mtisw` solo se usa si el profesor entrega esa cuenta.

`DOCKERHUB_TOKEN` es un token de DockerHub con permiso `Read & Write`.

Estos valores se configuraron como `Repository secrets`, no como `Environment secrets`, porque el workflow los consume directamente desde el repositorio:

```text
secrets.DOCKERHUB_USERNAME
secrets.DOCKERHUB_TOKEN
```

Para el token, cree un Personal Access Token en DockerHub dedicado al pipeline de GitHub Actions, con permiso `Read & Write`. Esto evita usar la contrasena de DockerHub y permite revocar solo ese token si alguna vez se necesita.

### Variables recomendadas

Como el frontend usa Vite, la URL de Keycloak queda incorporada al momento de construir la imagen. Por eso, si quiero que GitHub Actions publique una imagen lista para EC2, debo definir:

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

Si cambia la IP publica de EC2, se debe actualizar `VITE_KEYCLOAK_URL` antes de publicar una nueva imagen frontend.

Estas variables se configuraron como `Repository variables`, no como `Environment variables`, porque el proyecto no separa ambientes formales como `staging` y `production`. La variable mas importante es `VITE_KEYCLOAK_URL`, ya que Vite la incorpora dentro del build estatico del frontend.

### Pasos finales para que el pipeline quede en success

Despues de configurar secrets y variables, los pasos finales son:

```bash
git checkout -b feature/github-actions-ci
git add .github/workflows docs/GIT_GITHUB_ACTIONS.md docker-compose.yml keycloak/hotelrm-realm.json Frontend/package.json Frontend/package-lock.json GUIA_DEFENSA_PROYECTO.md
git commit -m "ci: add github actions docker publish workflow"
git push -u origin feature/github-actions-ci
```

Luego se crea un Pull Request hacia `develop` o `main`, segun la estrategia que se quiera demostrar. El workflow `CI` debe quedar en verde porque valida:

- pruebas del backend con Java 21 y Maven
- lint del frontend
- build del frontend

Para publicar imagenes Docker se debe crear un tag semantico:

```bash
git checkout main
git pull origin main
git tag v1.0.0
git push origin v1.0.0
```

Ese tag activa `Docker Publish` y sube las imagenes:

```text
je4nn/travelagency-backend
je4nn/travelagency-frontend
```

### Ajuste de compatibilidad con AWS

El despliegue real en AWS funciono correctamente usando:

```text
Keycloak 21.1.2
keycloak-js 21.1.2
PKCE S256
sslRequired=none
```

Por eso deje el repositorio alineado con esa configuracion. Esto evita que GitHub Actions publique imagenes con la version nueva de Keycloak que habia causado problemas al iniciar sesion desde HTTP usando IP publica.

### Como explicarlo al profesor

> “Ademas del pipeline de Jenkins, agregue GitHub Actions para tener CI/CD remoto. El workflow de CI ejecuta pruebas y build en cada cambio, mientras que el workflow de Docker publica imagenes en DockerHub solo cuando creo un tag semantico como `v1.0.0`. Asi separo validacion continua de publicacion de releases, evito gastar ejecuciones innecesarias y mantengo credenciales protegidas mediante secrets.”

---

## 21. Respuestas estrategicas si te hacen preguntas dificiles

### Si preguntan “¿que parte consideras mas importante?”

Puedes responder:

> “La integracion completa entre frontend, backend y seguridad con Keycloak, porque une interfaz, logica de negocio, persistencia y autorizacion real.”

### Si preguntan “¿que parte fue mas compleja?”

Puedes responder:

> “La seguridad y la consistencia entre frontend, backend, roles, CORS y redirecciones de Keycloak.”

### Si preguntan “¿que mejorarias a futuro?”

Puedes responder:

> “Mas cobertura en servicios complejos, mas validaciones de negocio, y una version completa de alta disponibilidad con balanceadores en Docker Compose.”

### Si preguntan “¿que libertades tomaste respecto a clases?”

Puedes responder:

> “Principalmente PostgreSQL en vez de MySQL y ajustes de puertos/entorno, pero manteniendo la misma arquitectura, el mismo flujo full stack y los mismos conceptos del curso.”

---

## 22. Cierre corto para decir al final

> “El proyecto implementa una aplicacion web completa con frontend y backend desacoplados, persistencia relacional, autenticacion centralizada con Keycloak, despliegue con Docker, automatizacion con Jenkins y pruebas con cobertura medida por JaCoCo.”
