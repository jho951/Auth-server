#!/bin/bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
GRADLE_PROPERTIES_FILE="$PROJECT_ROOT/gradle.properties"
GENERATED_ENV_DIR="$SCRIPT_DIR/.generated"

ENV=${1:-dev}
TARGET=${2:-all}
ENV_FILE="$GENERATED_ENV_DIR/.env.$ENV"

APP_COMPOSE_FILES=(
  "$SCRIPT_DIR/docker-compose.app.yml"
  "$SCRIPT_DIR/services/mysql/$ENV/docker-compose.mysql.yml"
)

case "$TARGET" in
  all|app)
    COMPOSE_FILES=("${APP_COMPOSE_FILES[@]}")
    ;;
  *)
    echo "❌ Invalid target: $TARGET"
    echo "ℹ️  Usage: ./docker/start.sh [dev|prod] [all|app]"
    exit 1
    ;;
esac

[[ -n "${ES_COMPOSE:-}" ]] && COMPOSE_FILES+=("$ES_COMPOSE")

generate_env_file() {
  local prefix="env.${ENV}."
  mkdir -p "$GENERATED_ENV_DIR"

  if [[ ! -f "$GRADLE_PROPERTIES_FILE" ]]; then
    echo "❌ gradle.properties not found: $GRADLE_PROPERTIES_FILE"
    exit 1
  fi

  awk -v prefix="$prefix" '
    index($0, prefix) == 1 {
      line = substr($0, length(prefix) + 1)
      eq = index(line, "=")
      if (eq == 0) {
        next
      }
      key = substr(line, 1, eq - 1)
      value = substr(line, eq + 1)
      print key "=" value
    }
  ' "$GRADLE_PROPERTIES_FILE" > "$ENV_FILE"

  if [[ ! -s "$ENV_FILE" ]]; then
    echo "❌ No environment entries found for prefix: $prefix"
    exit 1
  fi

  printf 'ENV_FILE_PATH=%s\n' "$ENV_FILE" >> "$ENV_FILE"
}

generate_env_file

if [[ ! -f "$ENV_FILE" ]]; then
  echo "❌ Generated ENV file not found: $ENV_FILE"
  exit 1
fi

# compose 파일 존재 확인 (빈 값 방지)
missing_files=()
for f in "${COMPOSE_FILES[@]}"; do
  if [[ -z "${f:-}" ]]; then
    missing_files+=("<empty-entry-in-COMPOSE_FILES>")
    continue
  fi
  [[ -f "$f" ]] || missing_files+=("$f")
done

if (( ${#missing_files[@]} > 0 )); then
  echo "❌ Missing compose files:"
  for f in "${missing_files[@]}"; do echo "  $f"; done
  echo "ℹ️  Check ENV='$ENV', directory names, and optional ES_COMPOSE."
  exit 1
fi

echo "✅ Environment: $ENV"
echo "✅ Target: $TARGET"
echo "✅ Using ENV file: $ENV_FILE"
echo "✅ Using Docker Compose files:"
for f in "${COMPOSE_FILES[@]}"; do echo "  $f"; done

# 실행
docker compose --env-file "$ENV_FILE" \
  $(for f in "${COMPOSE_FILES[@]}"; do echo -n "-f $f "; done) \
  up --build -d
