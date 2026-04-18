#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
KEYCLOAK_HOME="${KEYCLOAK_HOME:-$HOME/Descargas/keycloak-26.6.1}"
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8080}"
KEYCLOAK_ADMIN_REALM="${KEYCLOAK_ADMIN_REALM:-master}"
KEYCLOAK_ADMIN="${KEYCLOAK_ADMIN:-admin}"
KEYCLOAK_ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin}"
TARGET_REALM="${TARGET_REALM:-hotelrm}"
DEV_USERNAME="${DEV_USERNAME:-hotelrm-admin}"
DEV_PASSWORD="${DEV_PASSWORD:-changeit}"
DEV_EMAIL="${DEV_EMAIL:-hotelrm-admin@local.dev}"
DEV_FIRST_NAME="${DEV_FIRST_NAME:-HotelRM}"
DEV_LAST_NAME="${DEV_LAST_NAME:-Admin}"
DEV_ROLE="${DEV_ROLE:-hotelrm_admin}"
KCADM="$KEYCLOAK_HOME/bin/kcadm.sh"

if [[ ! -x "$KCADM" ]]; then
  echo "No se encontro kcadm.sh en $KCADM" >&2
  exit 1
fi

"$KCADM" config credentials \
  --server "$KEYCLOAK_URL" \
  --realm "$KEYCLOAK_ADMIN_REALM" \
  --user "$KEYCLOAK_ADMIN" \
  --password "$KEYCLOAK_ADMIN_PASSWORD" >/dev/null

lookup_user_id() {
  "$KCADM" get users -r "$TARGET_REALM" -q "username=$DEV_USERNAME" --fields id,username \
    | grep -m1 '"id"' \
    | sed -E 's/.*"id" : "([^"]+)".*/\1/' || true
}

USER_ID="$(lookup_user_id)"

if [[ -z "$USER_ID" ]]; then
  "$KCADM" create users -r "$TARGET_REALM" \
    -s "username=$DEV_USERNAME" \
    -s "enabled=true" \
    -s "emailVerified=true" \
    -s "email=$DEV_EMAIL" \
    -s "firstName=$DEV_FIRST_NAME" \
    -s "lastName=$DEV_LAST_NAME" >/dev/null

  USER_ID="$(lookup_user_id)"
  echo "Usuario $DEV_USERNAME creado."
else
  echo "Usuario $DEV_USERNAME ya existia."
fi

"$KCADM" set-password -r "$TARGET_REALM" --username "$DEV_USERNAME" --new-password "$DEV_PASSWORD" >/dev/null
"$KCADM" add-roles -r "$TARGET_REALM" --uusername "$DEV_USERNAME" --rolename "$DEV_ROLE" >/dev/null

echo "Usuario listo."
echo "username=$DEV_USERNAME"
echo "password=$DEV_PASSWORD"
