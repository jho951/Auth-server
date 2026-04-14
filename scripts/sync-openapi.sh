#!/usr/bin/env bash
set -euo pipefail

CONTRACT_ROOT="${CONTRACT_ROOT:-/Users/jhons/Downloads/BE/contract/service-contract}"
SOURCE="${CONTRACT_ROOT}/contracts/openapi/auth-service.upstream.v1.yaml"
TARGET="docs/openapi/auth-service.yml"

if [[ ! -f "${SOURCE}" ]]; then
  echo "OpenAPI source not found: ${SOURCE}" >&2
  exit 1
fi

cp "${SOURCE}" "${TARGET}"
echo "Synced ${TARGET} from ${SOURCE}"
