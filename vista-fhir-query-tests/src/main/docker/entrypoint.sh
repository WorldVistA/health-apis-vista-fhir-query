#!/usr/bin/env bash

set -euo pipefail

test -n "${K8S_ENVIRONMENT}"
test -n "${K8S_LOAD_BALANCER}"

if [ -z "${SENTINEL_BASE_DIR:-}" ]; then SENTINEL_BASE_DIR=/sentinel; fi
cd $SENTINEL_BASE_DIR

# =~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=

main() {
  if [ -z "${SENTINEL_ENV:-}" ]; then SENTINEL_ENV="${K8S_ENVIRONMENT}"; fi
  if [ -z "${VFQ_URL:-}" ]; then VFQ_URL="https://${K8S_LOAD_BALANCER}"; fi

  SYSTEM_PROPERTIES="-Dsentinel=${SENTINEL_ENV} \
    -Dsentinel.internal.url=${VFQ_URL} \
    -Dsentinel.r4.url=${VFQ_URL}"

  if [ -n "${VFQ_API_PATH:-}" ]; then  addToSystemProperties "sentinel.internal.api-path" "${VFQ_API_PATH}"; fi
  if [ -n "${VFQ_R4_API_PATH:-}" ]; then  addToSystemProperties "sentinel.r4.api-path" "${VFQ_R4_API_PATH}"; fi
  if [ -n "${VFQ_PORT:-}" ]; then addToSystemProperties "sentinel.internal.port" "${VFQ_PORT}"; fi
  if [ -n "${VFQ_R4_PORT:-}" ]; then addToSystemProperties "sentinel.r4.port" "${VFQ_R4_PORT}"; fi
  if [ -n "${MAGIC_ACCESS_TOKEN:-}" ]; then addToSystemProperties "access-token" "${MAGIC_ACCESS_TOKEN}"; fi
  if [ -n "${VFQ_CLIENT_KEY:-}" ]; then addToSystemProperties "client-key" "${VFQ_CLIENT_KEY}"; fi

  java-tests \
    --module-name "vista-fhir-query-tests" \
    --regression-test-pattern ".*IT\$" \
    --smoke-test-pattern ".*PingIT\$" \
    $SYSTEM_PROPERTIES \
    $@

  exit $?
}

# =~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=

addToSystemProperties() {
  SYSTEM_PROPERTIES+=" -D$1=$2"
}

# =~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=

main $@
