#!/usr/bin/env bash

set -ex

source ./data_baremetal.sh
source ./data_gc.sh

# Bootstrap Partition service on Baremetal (on-prem)
bootstrap_baremetal() {

  DATA_PARTITION_ID=$1
  DATA_PARTITION_ID_UPPER=$2

  status_code=$(curl -X POST \
    --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
    -H "Content-Type: application/json" \
    --data-raw "$(generate_post_data_baremetal)")

  # shellcheck disable=SC2002
  if [[ "${status_code}" == 201 ]]; then
    echo "Partition bootstrap finished successfully!"
  elif [[ "${status_code}" == 409 ]]; then

    patch_status_code=$(curl -X PATCH \
    --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
    -H "Content-Type: application/json" \
    --data-raw "$(generate_post_data_baremetal)")

    echo "Partition was patched because Postgres Database had already had entities! Status code: ${patch_status_code}"
  else
    echo "Exiting with status code: ${status_code}"
    exit 1
  fi
}

# Bootstrap Partition service on Google Cloud
bootstrap_gc() {

  echo "sleep to prevent 500 response from the Partition service, due to timeout of creation for Workload Identity"
  sleep 20

  DATA_PARTITION_ID=$1
  DATA_PARTITION_ID_UPPER=$2

  status_code=$(curl -X POST \
     --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
     -H "Content-Type: application/json" \
     --data-raw "$(generate_post_data_gc)")

  # shellcheck disable=SC2002
  if [[ "${status_code}" == 201 ]]; then
    echo "Partition bootstrap finished successfully!"
  elif [[ "${status_code}" == 409 ]]; then

    patch_status_code=$(curl -X PATCH \
    --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
    -H "Content-Type: application/json" \
    --data-raw "$(generate_post_data_gc)")

    echo "Partition was patched because Datastore had already had entities! Status code: ${patch_status_code}"
  else
    echo "Exiting with status code: ${status_code}"
    exit 1
  fi
}

if [[ "${ENVIRONMENT}" == "anthos" && "${DATA_PARTITION_ID_LIST}" == "" ]]; then
  bootstrap_baremetal "${DATA_PARTITION_ID}" "${DATA_PARTITION_ID^^}"
elif [[ "${ENVIRONMENT}" == "gcp" && "${DATA_PARTITION_ID_LIST}" == "" ]]; then
  bootstrap_gc "${DATA_PARTITION_ID}" "${DATA_PARTITION_ID^^}"
elif [[ "${ENVIRONMENT}" == "gcp" && "${DATA_PARTITION_ID_LIST}" != "" ]]; then

  IFS=',' read -ra PARTITIONS <<< "${DATA_PARTITION_ID_LIST}"
  PARTITIONS=("${DATA_PARTITION_ID}" "${PARTITIONS[@]}")

  for PARTITION in "${PARTITIONS[@]}"; do
    bootstrap_gc "${PARTITION}" "${PARTITION^^}"
  done
fi

touch /tmp/bootstrap_ready
