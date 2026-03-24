#!/bin/bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

ENV=${1:-dev}
ENV_FILE="$PROJECT_ROOT/.env.$ENV"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Source env file not found: $ENV_FILE"
  exit 1
fi

echo "Environment: $ENV"
echo "Using source env file: $ENV_FILE"

set -a
source "$ENV_FILE"
set +a

cd "$PROJECT_ROOT"
./gradlew :app:bootRun --args="--spring.profiles.active=$ENV"
