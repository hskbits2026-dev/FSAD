#!/usr/bin/env bash
# Starts the backend with secrets from .env loaded into the environment.
# Usage: ./run-local.sh
set -euo pipefail
cd "$(dirname "$0")"

if [[ -f .env ]]; then
  set -a            # auto-export every var we source
  source .env
  set +a
fi

exec ./mvnw spring-boot:run
