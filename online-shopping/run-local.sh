#!/usr/bin/env bash
# Starts the backend with secrets from .env loaded, and (if the Stripe CLI is
# available) a `stripe listen` process in the background to forward webhooks.
# The listener is killed automatically when this script exits (Ctrl-C).
# Usage: ./run-local.sh
#
# Still start these yourself, separately:
#   - MySQL on :3306   (e.g. `brew services start mysql`)
#   - the frontend     (`cd frontend && npm run dev`)
set -euo pipefail
cd "$(dirname "$0")"

if [[ -f .env ]]; then
  set -a            # auto-export every var we source
  source .env
  set +a
fi

# --- Stripe webhook forwarder (background) ---------------------------------
# Only starts if the `stripe` CLI is installed. If it isn't, the backend still
# runs — you just won't get PAID-order webhooks (checkout page still works).
STRIPE_PID=""
if command -v stripe >/dev/null 2>&1; then
  echo "▶ starting stripe listen (webhook forwarder)…"
  # --api-key binds the listener to the SAME Stripe account the secret key belongs to.
  # Without it, `stripe login` may point at a different account and no events ever arrive.
  stripe listen --api-key "$STRIPE_SECRET_KEY" \
    --events checkout.session.completed \
    --forward-to localhost:8081/api/stripe/webhook &
  STRIPE_PID=$!
else
  echo "⚠ stripe CLI not found — skipping webhook forwarder (checkout works, PAID/email won't)."
fi

# When this script stops, also stop the background listener.
cleanup() {
  if [[ -n "$STRIPE_PID" ]] && kill -0 "$STRIPE_PID" 2>/dev/null; then
    echo "■ stopping stripe listen…"
    kill "$STRIPE_PID" 2>/dev/null || true
  fi
}
trap cleanup EXIT

# Backend in the foreground (keeps the script alive; Ctrl-C triggers cleanup).
./mvnw spring-boot:run
