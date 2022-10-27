#!/usr/bin/env bash

set -ex

source ./data_anthos.sh
source ./data_gcp.sh

# Bootstrap Partition service on Anthos (on-prem)
bootstrap_anthos() {

  DATA_PARTITION_ID=$1
  DATA_PARTITION_ID_UPPER=$2

  if [[ "${PARTITION_CLEAN_UP_ENABLED}" == "true" ]]; then
    echo "Partition cleanup enabled, will delete partition ${DATA_PARTITION_ID}"

    delete_status_code=$(curl -X DELETE \
      --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
      -H "Content-Type: application/json")

    if [[ "${delete_status_code}" == 204 ]] || [[ "${delete_status_code}" == 404 ]]; then
      echo "Partition deletion was successful with status code: ${delete_status_code}"
    else
      echo "Not able to delete partition with status code: ${delete_status_code}"
      exit 1
    fi
  else
    echo "Partition cleanup is not enabled, skipping deletion"
  fi

  status_code=$(curl -X POST \
    --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
    -H "Content-Type: application/json" \
    --data-raw "$(generate_post_data_anthos)")

  # shellcheck disable=SC2002
  if [[ "${status_code}" == 201 ]]; then
    echo "Partition bootstrap finished successfully!"
  elif [[ "${status_code}" == 409 ]]; then

    patch_status_code=$(curl -X PATCH \
    --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
    -H "Content-Type: application/json" \
    --data-raw "$(generate_post_data_anthos)")

    echo "Partition was patched because Postgres Database had already had entities! Status code: ${patch_status_code}"
  else
    echo "Exiting with status code: ${status_code}"
    exit 1
  fi
}

# Bootstrap Partition service on Google Cloud
bootstrap_gcp() {

  echo "sleep to prevent 500 response from the Partition service, due to timeout of creation for Workload Identity"
  sleep 20

  DATA_PARTITION_ID=$1
  DATA_PARTITION_ID_UPPER=$2
  IDENTITY_TOKEN=$(gcloud auth print-identity-token --audiences="${AUDIENCES}")

  if [[ "${PARTITION_CLEAN_UP_ENABLED}" == "true" ]]; then
    echo "Partition cleanup enabled, will delete partition ${DATA_PARTITION_ID}"

    delete_status_code=$(curl -X DELETE \
      --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
      -H "Authorization: Bearer ${IDENTITY_TOKEN}")

    if [[ "${delete_status_code}" == 204 ]] || [[ "${delete_status_code}" == 404 ]]; then
      echo "Partition deletion was successful with status code: ${delete_status_code}"
    else
      echo "Not able to delete partition with status code: ${delete_status_code}"
      exit 1
    fi
  else
    echo "Partition cleanup not enabled, skipping deletion"
  fi

  status_code=$(curl -X POST \
     --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
     -H "Authorization: Bearer ${IDENTITY_TOKEN}" \
     -H "Content-Type: application/json" \
     --data-raw "$(generate_post_data_gcp)")

  # shellcheck disable=SC2002
  if [[ "${status_code}" == 201 ]]; then
    echo "Partition bootstrap finished successfully!"
  elif [[ "${status_code}" == 409 ]]; then

    patch_status_code=$(curl -X PATCH \
    --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
    -H "Authorization: Bearer ${IDENTITY_TOKEN}" \
    -H "Content-Type: application/json" \
    --data-raw "$(generate_post_data_gcp)")

    echo "Partition was patched because Datastore had already had entities! Status code: ${patch_status_code}"
  else
    echo "Exiting with status code: ${status_code}"
    exit 1
  fi
}

if [[ "${ENVIRONMENT}" == "anthos" && "${DATA_PARTITION_ID_LIST}" == "" ]]; then
  bootstrap_anthos "${DATA_PARTITION_ID}" "${DATA_PARTITION_ID^^}"
elif [[ "${ENVIRONMENT}" == "gcp" && "${DATA_PARTITION_ID_LIST}" == "" ]]; then
  bootstrap_gcp "${DATA_PARTITION_ID}" "${DATA_PARTITION_ID^^}"
elif [[ "${ENVIRONMENT}" == "gcp" && "${DATA_PARTITION_ID_LIST}" != "" ]]; then

  IFS=',' read -ra PARTITIONS <<< "${DATA_PARTITION_ID_LIST}"
  PARTITIONS=("${DATA_PARTITION_ID}" "${PARTITIONS[@]}")

  for PARTITION in "${PARTITIONS[@]}"; do
    bootstrap_gcp "${PARTITION}" "${PARTITION^^}"
  done
fi

touch /tmp/bootstrap_ready
