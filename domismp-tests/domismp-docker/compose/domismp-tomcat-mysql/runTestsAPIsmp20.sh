#!/bin/bash

# init plan variables
WORKDIR="$(cd -P $(dirname ${BASH_SOURCE[0]} ) && pwd)"
source "${WORKDIR}/../../functions/run-test.functions"
initializeVariables

RESULT_FOLDER="${WORKDIR}/results"
# clear old results
rm -rf "${RESULT_FOLDER}"
mkdir -p "${RESULT_FOLDER}"

# define network to connect the tests
DOCKER_NETWORK_NAME="${PLAN_PREFIX}_default"
export DOCKER_NETWORK_NAME

# Starting Docker Compose TEST (in specific project to avoid orphan container warning)
docker compose -f docker-compose.test-api-smp20.yml -p "run-${PLAN_PREFIX}" up
docker cp "run-${PLAN_PREFIX}-testapi-1:/data/results/soapui-reports" ./results
