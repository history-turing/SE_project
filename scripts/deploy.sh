#!/usr/bin/env bash

set -euo pipefail

ENV_FILE="${TREEHOLE_DEPLOY_ENV_FILE:-.env.production}"
COMPOSE_PROJECT="${TREEHOLE_COMPOSE_PROJECT:-se_project}"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "Missing env file: ${ENV_FILE}" >&2
  exit 1
fi

compose_cmd() {
  if docker compose version >/dev/null 2>&1; then
    docker compose --env-file "${ENV_FILE}" -p "${COMPOSE_PROJECT}" "$@"
  else
    docker-compose --env-file "${ENV_FILE}" -p "${COMPOSE_PROJECT}" "$@"
  fi
}

service_container_id() {
  local service="$1"
  compose_cmd ps -q "${service}" | tail -n 1
}

service_status() {
  local service="$1"
  local container_id
  container_id="$(service_container_id "${service}")"
  if [[ -z "${container_id}" ]]; then
    echo "missing"
    return 1
  fi

  docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "${container_id}"
}

wait_for_service() {
  local service="$1"
  local expected_status="$2"
  local attempts="${3:-24}"
  local sleep_seconds="${4:-5}"
  local current_status=""

  for ((attempt = 1; attempt <= attempts; attempt += 1)); do
    current_status="$(service_status "${service}" 2>/dev/null || true)"
    if [[ "${current_status}" == "${expected_status}" ]]; then
      return 0
    fi
    sleep "${sleep_seconds}"
  done

  echo "Service ${service} did not reach status ${expected_status}. Last status: ${current_status:-unknown}" >&2
  compose_cmd ps
  compose_cmd logs "${service}" --tail=200 || true
  return 1
}

read_env_value() {
  local key="$1"
  local value
  value="$(grep -E "^${key}=" "${ENV_FILE}" | tail -n 1 | cut -d'=' -f2- || true)"
  if [[ -z "${value}" ]]; then
    echo "Missing required key ${key} in ${ENV_FILE}" >&2
    exit 1
  fi
  printf '%s' "${value}"
}

MYSQL_DATABASE="$(read_env_value MYSQL_DATABASE)"
MYSQL_ROOT_PASSWORD="$(read_env_value MYSQL_ROOT_PASSWORD)"

compose_cmd up -d --build mysql redis

wait_for_service mysql healthy 36 5
wait_for_service redis healthy 24 5

compose_cmd exec -T mysql mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" -D "${MYSQL_DATABASE}" \
  < backend/whu-treehole-server/src/main/resources/db/schema.sql
compose_cmd exec -T mysql mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" -D "${MYSQL_DATABASE}" \
  < backend/whu-treehole-server/src/main/resources/db/data.sql

compose_cmd up -d --build backend frontend

wait_for_service backend healthy 36 5
wait_for_service frontend healthy 24 5

compose_cmd ps
