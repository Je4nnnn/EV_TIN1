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

## 19. Respuestas estrategicas si te hacen preguntas dificiles

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

## 20. Cierre corto para decir al final

> “El proyecto implementa una aplicacion web completa con frontend y backend desacoplados, persistencia relacional, autenticacion centralizada con Keycloak, despliegue con Docker, automatizacion con Jenkins y pruebas con cobertura medida por JaCoCo.”

