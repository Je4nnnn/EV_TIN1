# Despliegue en AWS EC2

Esta guia deja el proyecto corriendo en una sola instancia EC2 usando Docker Compose. Es la ruta mas simple para la entrega: un Nginx publico en puerto 80, PostgreSQL y Keycloak en contenedores, y tres replicas del backend detras del Nginx del proyecto.

## Instancia recomendada

- AMI: Ubuntu Server 24.04 LTS o Amazon Linux 2023.
- Tipo: `t3.medium` o `t3a.medium` para demo estable, porque Keycloak + PostgreSQL + 3 backends Java consumen mas memoria que una `micro`.
- Disco: 20 GB `gp3`.
- Security Group:
  - SSH `22`: solo tu IP.
  - HTTP `80`: `0.0.0.0/0`.
  - Keycloak `8080`: `0.0.0.0/0` para demo. En produccion se debe cerrar o poner detras de HTTPS/proxy.
  - Backend `8091`: no es necesario abrirlo si usas `/api` por Nginx.

## Preparar la instancia

En Ubuntu Server 24.04 LTS:

```bash
sudo apt update
sudo apt install -y git ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo tee /etc/apt/keyrings/docker.asc >/dev/null
sudo chmod a+r /etc/apt/keyrings/docker.asc
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list >/dev/null
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker "$USER"
```

Cierra la sesion SSH y vuelve a entrar para que el grupo `docker` aplique.

En Amazon Linux 2023:

```bash
sudo dnf update -y
sudo dnf install -y git docker
sudo systemctl enable --now docker
sudo usermod -aG docker ec2-user
mkdir -p ~/.docker/cli-plugins
curl -SL https://github.com/docker/compose/releases/download/v5.1.2/docker-compose-linux-x86_64 -o ~/.docker/cli-plugins/docker-compose
chmod +x ~/.docker/cli-plugins/docker-compose
docker compose version
```

Cierra la sesion SSH y vuelve a entrar para que el grupo `docker` aplique.

## Subir y configurar el proyecto

```bash
git clone <URL_DEL_REPO> EV_TIN1
cd EV_TIN1
cp .env.aws.example .env
nano .env
```

En `.env`, reemplaza cada `<EC2_PUBLIC_IP>` por la IP publica real de la instancia. Si usas un dominio, usa el dominio en vez de la IP.

## Levantar servicios

```bash
docker compose up --build -d
docker compose ps
```

Validaciones rapidas:

```bash
curl -i http://localhost/actuator/health
curl -i http://localhost
docker compose logs --tail=80 backend1
docker compose logs --tail=80 keycloak
```

La aplicacion queda en:

- Frontend: `http://<EC2_PUBLIC_IP>/home`
- API via Nginx: `http://<EC2_PUBLIC_IP>/api/...`
- Keycloak: `http://<EC2_PUBLIC_IP>:8080`
- Health backend: `http://<EC2_PUBLIC_IP>/actuator/health`

## Ajuste obligatorio en Keycloak

El realm importado trae redirects locales. En AWS debes agregar la URL publica del frontend:

1. Entra a `http://<EC2_PUBLIC_IP>:8080/admin`.
2. Login: `admin` / el valor de `KEYCLOAK_ADMIN_PASSWORD`.
3. Realm: `hotelrm`.
4. Clients -> `hotelrm-frontend`.
5. Agrega en Valid redirect URIs:
   - `http://<EC2_PUBLIC_IP>/*`
6. Agrega en Web origins:
   - `http://<EC2_PUBLIC_IP>`
7. Guarda.

Luego prueba login en `http://<EC2_PUBLIC_IP>/home`.

## Comandos utiles

```bash
docker compose logs -f nginx
docker compose logs -f backend1
docker compose logs -f keycloak
docker compose restart frontend backend1 backend2 backend3 nginx
docker compose down
docker compose down -v
```

Usa `docker compose down -v` solo si quieres borrar la base PostgreSQL y partir con datos demo limpios.

## Referencias

- Docker Engine en Ubuntu: https://docs.docker.com/engine/install/ubuntu/
- Docker Compose plugin en Linux: https://docs.docker.com/compose/install/linux/
- Docker en Amazon Linux 2023: https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-docker.html
