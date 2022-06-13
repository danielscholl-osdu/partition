#!/usr/bin/env bash

set -ex

DATA_PARTITION_ID_UPPER=${DATA_PARTITION_ID^^}

generate_post_data() {
  cat <<EOF
{
  "properties": {
    "projectId": {
      "sensitive": false,
      "value": "${PROJECT_ID}"
    },
    "serviceAccount": {
      "sensitive": false,
      "value": "${SERVICE_ACCOUNT}"
    },
    "complianceRuleSet": {
      "sensitive": false,
      "value": "shared"
    },
    "dataPartitionId": {
      "sensitive": false,
      "value": "${DATA_PARTITION_ID}"
    },
    "name": {
      "sensitive": false,
      "value": "${DATA_PARTITION_ID}"
    },
    "policy-service-enabled": {
      "sensitive": false,
      "value": "false"
    },
    "bucket": {
      "sensitive": false,
      "value": "${PROJECT_ID}-records"
    },
    "crmAccountID": {
      "sensitive": false,
      "value": "[${DATA_PARTITION_ID},${DATA_PARTITION_ID}]"
    },
    "osm.postgres.datasource.url": {
      "sensitive": true,
      "value": "POSTGRES_DATASOURCE_URL_${DATA_PARTITION_ID_UPPER}"
    },
    "osm.postgres.datasource.username": {
      "sensitive": true,
      "value": "POSTGRES_DB_USERNAME_${DATA_PARTITION_ID_UPPER}"
    },
    "osm.postgres.datasource.password": {
      "sensitive": true,
      "value": "POSTGRES_DB_PASSWORD_${DATA_PARTITION_ID_UPPER}"
    },
    "file.minio.endpoint": {
      "sensitive": false,
      "value": "https://s3.${DOMAIN}"
    },
    "file.minio.accessKey": {
      "sensitive": true,
      "value": "MINIO_ACCESS_KEY"
    },
    "file.minio.secretKey": {
      "sensitive": true,
      "value": "MINIO_SECRET_KEY"
    },
    "obm.minio.endpoint": {
      "sensitive": false,
      "value": "http://minio:9000"
    },
    "obm.minio.accessKey": {
      "sensitive": true,
      "value": "MINIO_ACCESS_KEY"
    },
    "obm.minio.secretKey": {
      "sensitive": true,
      "value": "MINIO_SECRET_KEY"
    },
    "oqm.rabbitmq.amqp.host": {
      "sensitive": false,
      "value": "rabbitmq"
    },
    "oqm.rabbitmq.amqp.port": {
      "sensitive": false,
      "value": "5672"
    },
    "oqm.rabbitmq.amqp.path": {
      "sensitive": false,
      "value": ""
    },
    "oqm.rabbitmq.amqp.username": {
      "sensitive": true,
      "value": "RABBITMQ_ADMIN_USERNAME"
    },
    "oqm.rabbitmq.amqp.password": {
      "sensitive": true,
      "value": "RABBITMQ_ADMIN_PASSWORD"
    },
    "oqm.rabbitmq.admin.schema": {
      "sensitive": false,
      "value": "http"
    },
    "oqm.rabbitmq.admin.host": {
      "sensitive": false,
      "value": "rabbitmq"
    },
    "oqm.rabbitmq.admin.port": {
      "sensitive": false,
      "value": "15672"
    },
    "oqm.rabbitmq.admin.path": {
      "sensitive": false,
      "value": "/api"
    },
    "oqm.rabbitmq.admin.username": {
      "sensitive": true,
      "value": "RABBITMQ_ADMIN_USERNAME"
    },
    "oqm.rabbitmq.admin.password": {
      "sensitive": true,
      "value": "RABBITMQ_ADMIN_PASSWORD"
    },
    "elasticsearch.host": {
      "sensitive": true,
      "value": "ELASTIC_HOST_${DATA_PARTITION_ID_UPPER}"
    },
    "elasticsearch.port": {
      "sensitive": true,
      "value": "ELASTIC_PORT_${DATA_PARTITION_ID_UPPER}"
    },
    "elasticsearch.user": {
      "sensitive": true,
      "value": "ELASTIC_USER_${DATA_PARTITION_ID_UPPER}"
    },
    "elasticsearch.password": {
      "sensitive": true,
      "value": "ELASTIC_PASS_${DATA_PARTITION_ID_UPPER}"
    }
  }
}
EOF
}

if [ "$ENVIRONMENT" == "anthos" ]
then

  status_code=$(curl -X POST \
    --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
    -H "Content-Type: application/json" \
    --data-raw "$(generate_post_data)")

  if [ "$status_code" == 201 ]
  then
    echo "Partition bootstrap finished successfully!"
  elif [ "$status_code" == 409 ]
  then
    curl -X PATCH \
    --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
    -H "Content-Type: application/json" \
    --data-raw "$(generate_post_data)"
    echo "Partition was patched because Postgres Database had already had entities!"
  else
    exit 1
  fi

elif [ "$ENVIRONMENT" == "gcp" ]
then

  echo "sleep to prevent 500 response from the Partition service, due to timeout of creation for Workload Identity"
  sleep 20

  IDENTITY_TOKEN=$(gcloud auth print-identity-token --audiences="${AUDIENCES}")

  status_code=$(curl -X POST \
     --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
     -H "Authorization: Bearer ${IDENTITY_TOKEN}" \
     -H "Content-Type: application/json" \
     --data-raw "$(generate_post_data)")

  if [ "$status_code" == 201 ]
  then
    echo "Partition bootstrap finished successfully!"
  elif [ "$status_code" == 409 ]
  then
    curl -X PATCH \
    --url "http://${PARTITION_NAME}/api/partition/v1/partitions/${DATA_PARTITION_ID}" --write-out "%{http_code}" --silent --output "/dev/null" \
    -H "Authorization: Bearer ${IDENTITY_TOKEN}" \
    -H "Content-Type: application/json" \
    --data-raw "$(generate_post_data)"
    echo "Partition was patched because Datastore had already had entities!"
  else
    exit 1
  fi
fi

touch /tmp/bootstrap_ready
