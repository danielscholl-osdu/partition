#!/usr/bin/env bash

set -ex

source ./data_anthos.sh
source ./data_gcp.sh

bootstrap_anthos() {
  if [ "$PARTITION_CLEAN_UP_ENABLED" == "true" ]
  then
    echo "Partition cleanup enabled, will delete partition ${DATA_PARTITION_ID}"
    delete_status_code=$(curl -X DELETE \
        --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
        -H "Content-Type: application/json")

      if [ "$delete_status_code" == 204 ] || [ "$delete_status_code" == 404 ]
      then
        echo "Partition deletion was successful, with status code: ${delete_status_code}"
      else
        echo "Not able to delete partition, status code is: ${delete_status_code}"
        exit 1
      fi
  else
    echo "Partition cleanup not enabled, skipping deletion"
  fi

  status_code=$(curl -X POST \
    --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
    -H "Content-Type: application/json" \
    --data-raw "$(generate_post_data_anthos)")

  # shellcheck disable=SC2002
  if [ "$status_code" == 201 ]
  then
    echo "Partition bootstrap finished successfully!"
  elif [ "$status_code" == 409 ]
  then
    patch_status_code=$(curl -X PATCH \
    --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
    -H "Content-Type: application/json" \
    --data-raw "$(generate_post_data_anthos)")
    echo "Partition was patched because Postgres Database had already had entities! Status code of patching: $patch_status_code"
  else
    echo "Exiting with status code: $status_code"
    exit 1
  fi
}

bootstrap_gcp() {
  echo "sleep to prevent 500 response from the Partition service, due to timeout of creation for Workload Identity"
  sleep 20

  IDENTITY_TOKEN=$(gcloud auth print-identity-token --audiences="${AUDIENCES}")

  if [ "$PARTITION_CLEAN_UP_ENABLED" == "true" ]
  then
      echo "Partition cleanup enabled, will delete partition ${DATA_PARTITION_ID}"
      delete_status_code=$(curl -X DELETE \
        --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
        -H "Authorization: Bearer ${IDENTITY_TOKEN}")

      if [ "$delete_status_code" == 204 ] || [ "$delete_status_code" == 404 ]
      then
        echo "Partition deletion was successful, with status code: ${delete_status_code}"
      else
        echo "Not able to delete partition, status code is: ${delete_status_code}"
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
  if [ "$status_code" == 201 ]
  then
    echo "Partition bootstrap finished successfully!"
  elif [ "$status_code" == 409 ]
  then
    patch_status_code=$(curl -X PATCH \
    --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
    -H "Authorization: Bearer ${IDENTITY_TOKEN}" \
    -H "Content-Type: application/json" \
    --data-raw "$(generate_post_data_gcp)")
    echo "Partition was patched because Datastore had already had entities! Status code of patching: $patch_status_code"
  else
    echo "Exiting with status code: $status_code"
    exit 1
  fi
}

if [ "$ENVIRONMENT" == "anthos" ]
then
  bootstrap_anthos
elif [ "$ENVIRONMENT" == "gcp" ]
then
  bootstrap_gcp
fi

touch /tmp/bootstrap_ready
