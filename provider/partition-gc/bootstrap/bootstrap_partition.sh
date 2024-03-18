#!/usr/bin/env bash

set -ex

source ./helpers.sh
source ./data_baremetal.sh
source ./data_gc.sh

# Bootstrap Partition service on Google Cloud
bootstrap_partition() {

  echo "sleep to prevent 500 response from the Partition service, due to timeout of creation for Workload Identity"
  sleep 20

  DATA_PARTITION_ID=$1
  BOOTSTRAP_DATA=$2
  
  echo "Bootstrapping partition: $DATA_PARTITION_ID"
  echo "$BOOTSTRAP_DATA" | jq

  status_code=$(curl -X POST \
     --url "http://${PARTITION_HOST}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
     -H "Content-Type: application/json" \
     --data-raw "$BOOTSTRAP_DATA")

  # shellcheck disable=SC2002
  if [[ "${status_code}" == 201 ]]; then
    echo "Partition bootstrap finished successfully!"
  elif [[ "${status_code}" == 409 ]]; then

    patch_status_code=$(curl -X PATCH \
    --url "http://${PARTITION_HOST}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
    -H "Content-Type: application/json" \
    --data-raw "$BOOTSTRAP_DATA")

    echo "Partition was patched because Datastore already has entities! Status code: ${patch_status_code}"
  else
    echo "Exiting with status code: ${status_code}"
    exit 1
  fi
}

# Bootstrap system partition
if [[ "${ENVIRONMENT}" == "gcp" ]]; then
  # Specifying "system" partition for GC installation 
  export SYSTEM_PARTITION_ID="system"
  export DATA_PARTITION_ID_VALUE="${SYSTEM_PARTITION_ID}"
  bootstrap_partition "${SYSTEM_PARTITION_ID}" "$(gc_system_partition_data)"
  
  # Bootstrap additional partition
  export DATA_PARTITION_ID_VALUE="${DATA_PARTITION_ID}"
  additional_partition_data=$(merge "gc_system_partition_data" "gc_additional_partition_data")
  bootstrap_partition "${DATA_PARTITION_ID}" "$additional_partition_data"
elif [[ "${ENVIRONMENT}" == "anthos" ]]; then
  export DATA_PARTITION_ID_VALUE="${DATA_PARTITION_ID}"
  bootstrap_partition "${DATA_PARTITION_ID}" "$(baremetal_system_partition_data)"
fi

touch /tmp/bootstrap_ready
