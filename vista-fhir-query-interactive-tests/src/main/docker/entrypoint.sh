#!/usr/bin/env bash
set -euo pipefail
# ==================================================

if [ -z "${SENTINEL_BASE_DIR:-}" ]; then SENTINEL_BASE_DIR=/sentinel; fi
cd $SENTINEL_BASE_DIR

# ==================================================

main() {
  SYSTEM_PROPERTIES=
  java-tests \
    --module-name "vista-fhir-query-test-suite" \
    $SYSTEM_PROPERTIES \
    $@

  exit $?
}

# ==================================================

main $@
