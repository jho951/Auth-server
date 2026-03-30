#!/bin/bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
DOCKER_DIR="$PROJECT_ROOT/docker"

ACTION=${1:-up}
ENV=${2:-dev}
TARGET=${3:-all}
STRICT_MODE=${4:-}
SOURCE_ENV_FILE="$PROJECT_ROOT/.env.$ENV"
EFFECTIVE_ENV_FILE="$SOURCE_ENV_FILE"
TEMP_ENV_FILE=""
MSA_SHARED_NETWORK="${MSA_SHARED_NETWORK:-msa-shared}"

APP_COMPOSE_FILES=(
  "$DOCKER_DIR/docker-compose.app.yml"
  "$DOCKER_DIR/services/mysql/$ENV/docker-compose.mysql.yml"
)

case "$TARGET" in
  all|app)
    COMPOSE_FILES=("${APP_COMPOSE_FILES[@]}")
    ;;
  *)
    echo "Invalid target: $TARGET"
    echo "Usage: ./scripts/run.docker.sh [up|down] [dev|prod] [all|app]"
    exit 1
    ;;
esac

case "$ACTION" in
  up|down)
    ;;
  *)
    echo "Invalid action: $ACTION"
    echo "Usage: ./scripts/run.docker.sh [up|down] [dev|prod] [all|app]"
    exit 1
    ;;
esac

[[ -n "${ES_COMPOSE:-}" ]] && COMPOSE_FILES+=("$ES_COMPOSE")

if [[ -n "$STRICT_MODE" && "$STRICT_MODE" != "--strict" ]]; then
  echo "Invalid option: $STRICT_MODE"
  echo "Usage: ./scripts/run.docker.sh [up|down] [dev|prod] [all|app] [--strict]"
  exit 1
fi

cleanup() {
  if [[ -n "$TEMP_ENV_FILE" && -f "$TEMP_ENV_FILE" ]]; then
    rm -f "$TEMP_ENV_FILE"
  fi
}
trap cleanup EXIT

write_fallback_env() {
  local env_file="$1"
  local profile="$2"
  cat > "$env_file" <<ENVEOF
SPRING_PROFILES_ACTIVE=$profile
MYSQL_DB=auth_service
MYSQL_USER=auth_service
MYSQL_PASSWORD=auth_service
MYSQL_ROOT_PASSWORD=root
MYSQL_HOST=auth-mysql
MYSQL_URL=jdbc:mysql://auth-mysql:3306/auth_service?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
REDIS_HOST=redis-server
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_SSL=false
SERVER_PORT=8081
INTERNAL_API_KEY=local-internal-api-key
USER_SERVICE_BASE_URL=http://user-service:8082
USER_SERVICE_INTERNAL_API_KEY=local-internal-api-key
USER_SERVICE_JWT_ISSUER=auth-service
USER_SERVICE_JWT_AUDIENCE=user-service
USER_SERVICE_JWT_SUBJECT=auth-service
USER_SERVICE_JWT_SCOPE=internal
USER_SERVICE_JWT_TTL_SECONDS=60
AUTH_JWT_SECRET=local-dev-auth-secret-local-dev-auth-secret
JWT_ACCESS_TOKEN_SECRET=local-dev-auth-secret-local-dev-auth-secret
AUTH_ACCESS_EXPIRATION=1200
AUTH_REFRESH_EXPIRATION=30000
ENVEOF
}

if [[ ! -f "$SOURCE_ENV_FILE" ]]; then
  if [[ "$STRICT_MODE" == "--strict" ]]; then
    echo "Source env file not found: $SOURCE_ENV_FILE"
    exit 1
  fi
  TEMP_ENV_FILE="$(mktemp "${TMPDIR:-/tmp}/auth-server-${ENV}.env.XXXXXX")"
  write_fallback_env "$TEMP_ENV_FILE" "$ENV"
  EFFECTIVE_ENV_FILE="$TEMP_ENV_FILE"
  echo "[WARN] $SOURCE_ENV_FILE not found. Using generated fallback env: $EFFECTIVE_ENV_FILE"
fi

docker network inspect "$MSA_SHARED_NETWORK" >/dev/null 2>&1 || docker network create "$MSA_SHARED_NETWORK" >/dev/null

missing_files=()
for f in "${COMPOSE_FILES[@]}"; do
  if [[ -z "${f:-}" ]]; then
    missing_files+=("<empty-entry-in-COMPOSE_FILES>")
    continue
  fi
  [[ -f "$f" ]] || missing_files+=("$f")
done

if (( ${#missing_files[@]} > 0 )); then
  echo "Missing compose files:"
  for f in "${missing_files[@]}"; do
    echo "  $f"
  done
  echo "Check ENV='$ENV', TARGET='$TARGET', and optional ES_COMPOSE."
  exit 1
fi

echo "Environment: $ENV"
echo "Target: $TARGET"
echo "Action: $ACTION"
echo "Using env file: $EFFECTIVE_ENV_FILE"
echo "Using Docker Compose files:"
for f in "${COMPOSE_FILES[@]}"; do
  echo "  $f"
done

COMPOSE_ARGS=()
for f in "${COMPOSE_FILES[@]}"; do
  COMPOSE_ARGS+=("-f" "$f")
done

if [[ "$ACTION" == "up" ]]; then
  ENV_FILE_PATH="$EFFECTIVE_ENV_FILE" MSA_SHARED_NETWORK="$MSA_SHARED_NETWORK" docker compose --env-file "$EFFECTIVE_ENV_FILE" \
    "${COMPOSE_ARGS[@]}" up --build -d
else
  ENV_FILE_PATH="$EFFECTIVE_ENV_FILE" MSA_SHARED_NETWORK="$MSA_SHARED_NETWORK" docker compose --env-file "$EFFECTIVE_ENV_FILE" \
    "${COMPOSE_ARGS[@]}" down --remove-orphans -v
fi
