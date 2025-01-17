#!/usr/bin/env bash

usage() {
  cat <<EOF

$0 [options]

Generate configurations for local development.

Options
     --debug               Enable debugging
 -h, --help                Print this help and exit.
     --secrets-conf <file> The configuration file with secrets!

Secrets Configuration
 This bash file is sourced and expected to set the following variables
- VFQ_DEFAULT_ACCESS_CODE
- VFQ_DEFAULT_VERIFY_CODE
- VFQ_673_ACCESS_CODE
- VFQ_673_VERIFY_CODE
- VFQ_673_APU
- VISTA_API_VPR_GET_PATIENT_DATA_CONTEXT

 Variables that can be used for optional configurations:
 - VISTALINK_CLIENT_KEY
 - WEB_EXCEPTION_KEY

$1
EOF
  exit 1
}

main() {
  REPO=$(cd $(dirname $0)/../.. && pwd)
  SECRETS="$REPO/secrets.conf"
  PROFILE=dev
  MARKER=$(date +%s)
  ARGS=$(getopt -n $(basename ${0}) \
      -l "debug,help,secrets-conf:" \
      -o "h" -- "$@")
  [ $? != 0 ] && usage
  eval set -- "$ARGS"
  while true
  do
    case "$1" in
      --debug) set -x ;;
      -h|--help) usage "halp! what this do?" ;;
      --secrets-conf) SECRETS="$2" ;;
      --) shift;break ;;
    esac
    shift;
  done

  echo "Loading secrets: $SECRETS"
  [ ! -f "$SECRETS" ] && usage "File not found: $SECRETS"
  . $SECRETS

  MISSING_SECRETS=false

  requiredParam VFQ_DEFAULT_ACCESS_CODE "${VFQ_DEFAULT_ACCESS_CODE}"
  requiredParam VFQ_DEFAULT_VERIFY_CODE "${VFQ_DEFAULT_VERIFY_CODE}"
  requiredParam VFQ_673_ACCESS_CODE "${VFQ_673_ACCESS_CODE}"
  requiredParam VFQ_673_VERIFY_CODE "${VFQ_673_VERIFY_CODE}"
  requiredParam VFQ_673_APU "${VFQ_673_APU}"
  requiredParam VISTA_API_VPR_GET_PATIENT_DATA_CONTEXT "${VISTA_API_VPR_GET_PATIENT_DATA_CONTEXT}"

  [ -z "${VISTA_API_URL:-}" ] && VISTA_API_URL="http://localhost:8050"
  [ -z "${VFQ_DB_URL:-}" ] && VFQ_DB_URL="jdbc:sqlserver://localhost:1433;database=dq;sendStringParametersAsUnicode=false"
  [ -z "${VFQ_DB_USER:-}" ] && VFQ_DB_USER="SA"
  [ -z "${VFQ_DB_PASSWORD:-}" ] && VFQ_DB_PASSWORD="<YourStrong!Passw0rd>"
  [ -z "$VISTALINK_CLIENT_KEY" ] && VISTALINK_CLIENT_KEY="not-used"
  [ -z "$WEB_EXCEPTION_KEY" ] && WEB_EXCEPTION_KEY="-shanktopus-for-the-win-"
  [ $MISSING_SECRETS == true ] && usage "Missing configuration secrets, please update ${SECRETS}"

  populateConfig
  populateRpcPrincipalFile
}

# =====================================================================

addValue() {
  local project="$1"
  local profile="$2"
  local key="$3"
  local value="$4"
  local target="$REPO/$project/config/application-${profile}.properties"
  local escapedValue=$(echo $value | sed -e 's/\\/\\\\/g; s/\//\\\//g; s/&/\\\&/g')
  echo "$key=$escapedValue" >> $target
}

checkForUnsetValues() {
  local project="$1"
  local profile="$2"
  local target="$REPO/$project/config/application-${profile}.properties"
  echo "checking $target"
  grep -E '(.*= *unset)' "$target"
  [ $? == 0 ] && echo "Failed to populate all unset values" && exit 1
  diff $target $target.$MARKER
  [ $? == 0 ] && rm -v $target.$MARKER
}

comment() {
  local project="$1"
  local profile="$2"
  local target="$REPO/$project/config/application-${profile}.properties"
  cat >> $target
}

configValue() {
  local project="$1"
  local profile="$2"
  local key="$3"
  local value="$4"
  local target="$REPO/$project/config/application-${profile}.properties"
  local escapedValue=$(echo $value | sed -e 's/\\/\\\\/g; s/\//\\\//g; s/&/\\\&/g')
  sed -i "s/^$key=.*/$key=$escapedValue/" $target
}

makeConfig() {
  local project="$1"
  local profile="$2"
  local target="$REPO/$project/config/application-${profile}.properties"
  [ -f "$target" ] && mv -v $target $target.$MARKER
  grep -E '(.*= *unset)' "$REPO/$project/src/main/resources/application.properties" \
    > "$target"
}
populateConfig() {
  makeConfig vista-fhir-query $PROFILE

  #Backend Health Checks
  configValue vista-fhir-query ${PROFILE} backend-health.charon.health-check-url "${VISTA_API_URL}/actuator/health"

  # Vista API Things
  configValue vista-fhir-query $PROFILE vista.api.url "${VISTA_API_URL}"
  comment vista-fhir-query $PROFILE <<EOF
# To populate vista.api.client-key, use the VISTALINK_CLIENT_KEY value in
# the secrets files used with make-configs.sh
EOF
  configValue vista-fhir-query $PROFILE vista.api.client-key "$VISTALINK_CLIENT_KEY"
  configValue vista-fhir-query $PROFILE vista.api.vpr-get-patient-data-context "${VISTA_API_VPR_GET_PATIENT_DATA_CONTEXT}"

  if [ -n "${VISTA_API_LOMA_LINDA_HACK_CONTEXT:-}" ]
  then
  configValue vista-fhir-query $PROFILE vista.api.loma-linda-hack-context "${VISTA_API_LOMA_LINDA_HACK_CONTEXT}"
  fi


  configValue vista-fhir-query $PROFILE vista-fhir-query.rpc-principals.file "config\/principals-$PROFILE.json"
  configValue vista-fhir-query $PROFILE vista-fhir-query.internal.client-keys "disabled"
  configValue vista-fhir-query $PROFILE vista-fhir-query.public-url "http://localhost:8095"
  configValue vista-fhir-query $PROFILE vista-fhir-query.public-r4-base-path "hcs/{site}/r4"
  addValue    vista-fhir-query $PROFILE vista-fhir-query.custom-r4-url-and-path.Endpoint "http://localhost:8095/r4"
  addValue    vista-fhir-query $PROFILE vista-fhir-query.custom-r4-url-and-path.Observation "http://localhost:8095/r4"
  addValue    vista-fhir-query $PROFILE vista-fhir-query.custom-r4-url-and-path.Patient "http://localhost:8090/data-query/r4"

  configValue vista-fhir-query $PROFILE vista-fhir-query.public-web-exception-key "$WEB_EXCEPTION_KEY"
  configValue vista-fhir-query $PROFILE ids-client.patient-icn.id-pattern "[0-9]+(V[0-9]{6})?"
  configValue vista-fhir-query $PROFILE ids-client.encoded-ids.encoding-key "fhir-query"
  configValue vista-fhir-query $PROFILE spring.datasource.url "${VFQ_DB_URL}"
  configValue vista-fhir-query $PROFILE spring.datasource.username "${VFQ_DB_USER}"
  configValue vista-fhir-query $PROFILE spring.datasource.password "${VFQ_DB_PASSWORD}"
  addValue vista-fhir-query $PROFILE management.endpoints.web.exposure.include "health,info,ids"

  # Alternate Patient ID Configs
  addValue vista-fhir-query $PROFILE alternate-patient-ids.enabled true
  addValue vista-fhir-query $PROFILE alternate-patient-ids.id.1011537977V693883 5000000347
  addValue vista-fhir-query $PROFILE alternate-patient-ids.id.32000225 195601
  addValue vista-fhir-query $PROFILE alternate-patient-ids.id.1017283148V813263 195604
  addValue vista-fhir-query $PROFILE alternate-patient-ids.id.5000335 195602
  addValue vista-fhir-query $PROFILE alternate-patient-ids.id.25000126 195603

  # Well-Known Configs
  configValue vista-fhir-query $PROFILE well-known.capabilities "context-standalone-patient, launch-ehr, permission-offline, permission-patient"
  configValue vista-fhir-query $PROFILE well-known.response-type-supported "code, refresh_token"
  configValue vista-fhir-query $PROFILE well-known.scopes-supported "patient/Observation.read, offline_access"

  # Metadata Configs
  configValue vista-fhir-query $PROFILE metadata.contact.email "$(sendMoarSpams)"
  configValue vista-fhir-query $PROFILE metadata.contact.name "$(whoDis)"
  configValue vista-fhir-query $PROFILE metadata.security.token-endpoint http://fake.com/token
  configValue vista-fhir-query $PROFILE metadata.security.authorize-endpoint http://fake.com/authorize
  configValue vista-fhir-query $PROFILE metadata.security.management-endpoint http://fake.com/manage
  configValue vista-fhir-query $PROFILE metadata.security.revocation-endpoint http://fake.com/revoke
  configValue vista-fhir-query $PROFILE metadata.statement-type patient

  configValue vista-fhir-query $PROFILE vista-fhir-query.mfq-base-url "http://localhost:8040"

  checkForUnsetValues vista-fhir-query $PROFILE
}

populateRpcPrincipalFile() {
  cat > $REPO/vista-fhir-query/config/principals-$PROFILE.json << EOF
{
    "entries" : [
        {
            "rpcNames" : [
                "VPR GET PATIENT DATA",
                "LHS LIGHTHOUSE RPC GATEWAY"
            ],
            "tags" : [
              "insurance-fhir-v0",
              "clinical-fhir-v0",
              "fugazi"
            ],
            "applicationProxyUser" : "LHS,APPLICATION PROXY",
            "codes" : [
                {
                    "sites" : [
                        "673"
                    ],
                    "accessCode" : "${VFQ_673_ACCESS_CODE}",
                    "verifyCode" : "${VFQ_673_VERIFY_CODE}"
                }
            ]
        },
        {
            "rpcNames" : [
                "IBLHS AMCMS GET INS"
            ],
            "tags" : [
              "fugazi"
            ],
            "applicationProxyUser" : "IBLHS,APPLICATION PROXY",
            "codes" : [
                {
                    "sites" : [
                        "673"
                    ],
                    "accessCode" : "${VFQ_673_ACCESS_CODE}",
                    "verifyCode" : "${VFQ_673_VERIFY_CODE}"
                }
            ]
        }
    ]
}
EOF
}

requiredParam() {
  local param="${1}"
  local value="${2:-}"
  if [ -z "$value" ]
  then
    usage "Missing Configuration: $param"
    MISSING_SECRETS=true
  fi
}

sendMoarSpams() {
  local spam=$(git config --global --get user.email)
  [ -z "$spam" ] && spam=$USER@aol.com
  echo $spam
}

whoDis() {
  local me=$(git config --global --get user.name)
  [ -z "$me" ] && me=$USER
  echo $me
}

# =====================================================================

main "$@"
