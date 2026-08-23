#!/bin/bash
set -euo pipefail

export DOMAIN_NAME="${DOMAIN_NAME:-todo_domain}"
export DOMAIN_HOME="/u01/oracle/user_projects/domains/${DOMAIN_NAME}"
export ADMIN_NAME="${ADMIN_NAME:-AdminServer}"
export PRODUCTION_MODE="${PRODUCTION_MODE:-dev}"
export ADMINISTRATION_PORT_ENABLED="${ADMINISTRATION_PORT_ENABLED:-false}"

PROPERTIES_FILE=/u01/oracle/properties/domain.properties

wait_for_db() {
  local host="${DB_HOST:-db}"
  local port="${DB_PORT:-1521}"
  echo "Waiting for Oracle at ${host}:${port}..."
  for _ in $(seq 1 60); do
    if bash -c "echo >/dev/tcp/${host}/${port}" 2>/dev/null; then
      echo "Oracle port is open"
      return 0
    fi
    sleep 5
  done
  echo "Oracle did not become reachable in time" >&2
  return 1
}

_term() {
  echo "SIGTERM received, shutting down WebLogic"
  if [ -x "${DOMAIN_HOME}/bin/stopWebLogic.sh" ]; then
    "${DOMAIN_HOME}/bin/stopWebLogic.sh" || true
  fi
}

trap _term SIGTERM

wait_for_db

if [ ! -f "${DOMAIN_HOME}/config/config.xml" ]; then
  if [ ! -f "${PROPERTIES_FILE}" ]; then
    echo "A properties file with the username and password needs to be supplied at ${PROPERTIES_FILE}"
    exit 1
  fi

  USER=$(awk '{print $1}' "${PROPERTIES_FILE}" | grep username | cut -d "=" -f2)
  PASS=$(awk '{print $1}' "${PROPERTIES_FILE}" | grep password | cut -d "=" -f2)
  if [ -z "${USER}" ] || [ -z "${PASS}" ]; then
    echo "Admin username and password must be set in ${PROPERTIES_FILE}"
    exit 1
  fi

  echo "Creating domain ${DOMAIN_NAME}"
  wlst.sh -skipWLSModuleScanning -loadProperties "${PROPERTIES_FILE}" /u01/oracle/create-wls-domain.py
  mkdir -p "${DOMAIN_HOME}/servers/${ADMIN_NAME}/security"
  echo "username=${USER}" > "${DOMAIN_HOME}/servers/${ADMIN_NAME}/security/boot.properties"
  echo "password=${PASS}" >> "${DOMAIN_HOME}/servers/${ADMIN_NAME}/security/boot.properties"
  wlst.sh -skipWLSModuleScanning /u01/oracle/container-scripts/create-datasource.py
fi

mkdir -p "${DOMAIN_HOME}/autodeploy"
cp /u01/oracle/apps/todo.war "${DOMAIN_HOME}/autodeploy/todo.war"

if [ -x "${DOMAIN_HOME}/startWebLogic.sh" ]; then
  exec "${DOMAIN_HOME}/startWebLogic.sh"
fi
exec "${DOMAIN_HOME}/bin/startWebLogic.sh"
