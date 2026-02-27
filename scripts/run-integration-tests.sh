#!/usr/bin/env bash
# Run ScalarDB integration tests against a temporary PostgreSQL container.
#
# Usage:
#   ./scripts/run-integration-tests.sh
#
# Requirements:
#   - Docker
#   - Leiningen

set -euo pipefail

CONTAINER_NAME="scarab-postgres-test"
POSTGRES_IMAGE="postgres:16"
POSTGRES_USER="postgres"
POSTGRES_PASSWORD="postgres"
POSTGRES_DB="postgres"
POSTGRES_PORT="5432"
SCALARDB_VERSION="3.17.1"
SCHEMA_LOADER_IMAGE="ghcr.io/scalar-labs/scalardb-schema-loader:${SCALARDB_VERSION}"
PROPERTIES_FILE=".github/scalardb/database.properties"

JDBC_URL="jdbc:postgresql://localhost:${POSTGRES_PORT}/${POSTGRES_DB}"

cleanup() {
  echo "--- Stopping and removing PostgreSQL container ---"
  docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true
  rm -f "${PROPERTIES_FILE}"
}
trap cleanup EXIT

echo "--- Starting PostgreSQL container ---"
docker run -d \
  --name "${CONTAINER_NAME}" \
  -e POSTGRES_USER="${POSTGRES_USER}" \
  -e POSTGRES_PASSWORD="${POSTGRES_PASSWORD}" \
  -e POSTGRES_DB="${POSTGRES_DB}" \
  -p "${POSTGRES_PORT}:5432" \
  "${POSTGRES_IMAGE}"

echo "--- Waiting for PostgreSQL to be ready ---"
for i in $(seq 1 30); do
  if docker exec "${CONTAINER_NAME}" pg_isready -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" >/dev/null 2>&1; then
    echo "PostgreSQL is ready."
    break
  fi
  if [ "$i" -eq 30 ]; then
    echo "PostgreSQL did not become ready in time." >&2
    exit 1
  fi
  sleep 2
done

echo "--- Writing ScalarDB properties ---"
cat > "${PROPERTIES_FILE}" <<EOF
scalar.db.storage=jdbc
scalar.db.contact_points=${JDBC_URL}
scalar.db.username=${POSTGRES_USER}
scalar.db.password=${POSTGRES_PASSWORD}
EOF

echo "--- Loading ScalarDB schema ---"
docker run --rm \
  --network host \
  -v "${PWD}/.github/scalardb:/scalardb/schema" \
  "${SCHEMA_LOADER_IMAGE}" \
  --config /scalardb/schema/database.properties \
  --schema-file /scalardb/schema/schema.json \
  --coordinator

echo "--- Running integration tests ---"
SCARAB_TEST_STORAGE=jdbc \
SCARAB_TEST_CONTACT_POINTS="${JDBC_URL}" \
SCARAB_TEST_USERNAME="${POSTGRES_USER}" \
SCARAB_TEST_PASSWORD="${POSTGRES_PASSWORD}" \
lein test :integration
