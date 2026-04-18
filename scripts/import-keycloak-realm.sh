#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
KEYCLOAK_HOME="${KEYCLOAK_HOME:-$HOME/Descargas/keycloak-26.6.1}"
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8080}"
KEYCLOAK_REALM="${KEYCLOAK_REALM:-master}"
KEYCLOAK_ADMIN="${KEYCLOAK_ADMIN:-admin}"
KEYCLOAK_ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin}"
REALM_FILE="${REALM_FILE:-$REPO_ROOT/keycloak/hotelrm-realm.json}"
KCADM="$KEYCLOAK_HOME/bin/kcadm.sh"

if [[ ! -x "$KCADM" ]]; then
  echo "No se encontro kcadm.sh en $KCADM" >&2
  echo "Define KEYCLOAK_HOME con la ruta correcta de Keycloak." >&2
  exit 1
fi

if [[ ! -f "$REALM_FILE" ]]; then
  echo "No se encontro el archivo de realm en $REALM_FILE" >&2
  exit 1
fi

"$KCADM" config credentials \
  --server "$KEYCLOAK_URL" \
  --realm "$KEYCLOAK_REALM" \
  --user "$KEYCLOAK_ADMIN" \
  --password "$KEYCLOAK_ADMIN_PASSWORD" >/dev/null

set +e
"$KCADM" get "realms/hotelrm" >/dev/null 2>&1
REALM_EXISTS=$?
set -e

if [[ $REALM_EXISTS -eq 0 ]]; then
  "$KCADM" update "realms/hotelrm" -f "$REALM_FILE" >/dev/null
  echo "Realm hotelrm actualizado."
else
  "$KCADM" create realms -f "$REALM_FILE" >/dev/null
  echo "Realm hotelrm creado."
fi

echo "Import terminado."
