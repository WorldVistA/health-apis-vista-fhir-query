#!/usr/bin/env bash
set -euo pipefail
# ==================================================

if [ -z "${SENTINEL_BASE_DIR:-}" ]; then SENTINEL_BASE_DIR=/sentinel; fi
cd $SENTINEL_BASE_DIR

# ==================================================

main() {

  SYSTEM_PROPERTIES=
  addToSystemProperties "interactive-tests" "true"

  local output="$(java-tests \
    --module-name "vista-fhir-query-interactive-tests" \
    list)"

  local prettyTestNames=("quit")
  for test in ${output}; do
    prettyTestNames+=("$(echo $test | sed -n -e 's/^.*interactivetests.//p')")
  done

  echo "Select a test to run from the below choices. 1 to quit."
  select choice in ${prettyTestNames[*]}; do
    if [[ $choice == "quit" ]]; then break; fi
    java-tests \
    run run \
    --module-name "vista-fhir-query-interactive-tests" \
    --test-pattern ".*$choice" \
    $SYSTEM_PROPERTIES
  done
  exit $?
}

addToSystemProperties() {
  SYSTEM_PROPERTIES+=" -D$1=$2"
}

# ==================================================

main $@
