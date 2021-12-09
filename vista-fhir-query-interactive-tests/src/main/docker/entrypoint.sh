#!/usr/bin/env bash
set -euo pipefail
# ==================================================

if [ -z "${SENTINEL_BASE_DIR:-}" ]; then SENTINEL_BASE_DIR=/sentinel; fi
cd $SENTINEL_BASE_DIR

# ==================================================

main() {

  echo "$@"
  SYSTEM_PROPERTIES=
  addToSystemProperties "interactive-tests" "true"
  addToSystemProperties "interactive-tests.test-properties" "/sentinel/test-properties"

  output="$(java-tests \
    --module-name "vista-fhir-query-interactive-tests" \
    list)"

  local prettyTestNames=("Quit")
  for test in ${output}; do
    prettyTestNames+=("$(echo $test | sed -n -e 's/^.*interactivetests.//p')")
  done

  echo "Select a test to run from the below choices. 1 to quit."
  local status=0
  while [ true ]
  do
    select choice in ${prettyTestNames[*]}
    do
      echo "$choice"
      if [ "${choice,,}" == "quit" ]; then return $status; fi
      if [[ "${choice}" != *Test ]]; then continue; fi
      if ! java-tests \
        run run \
        --module-name "vista-fhir-query-interactive-tests" \
        --test-pattern ".*$choice" \
        $SYSTEM_PROPERTIES \
        $@
      then
        echo "Failed to execute $choice"
        status=1
      fi
      break
    done
  done
  return $status
}

addToSystemProperties() {
  SYSTEM_PROPERTIES+=" -D$1=$2"
}

# ==================================================

main $@
