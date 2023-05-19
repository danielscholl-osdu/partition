#!/usr/bin/env bash

generate_post_data_baremetal() {
  cat <<EOF
{
  "properties": {
    "projectId": {
      "sensitive": false,
      "value": "${BUCKET_PREFIX}"
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
      "value": "${BUCKET_PREFIX}-${DATA_PARTITION_ID}-records"
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
    "obm.minio.endpoint": {
      "sensitive": false,
      "value": "${MINIO_ENDPOINT}"
    },
    "obm.minio.accessKey": {
      "sensitive": true,
      "value": "MINIO_ACCESS_KEY"
    },
    "obm.minio.secretKey": {
      "sensitive": true,
      "value": "MINIO_SECRET_KEY"
    },
    "obm.minio.ignoreCertCheck": {
      "sensitive": false,
      "value": "${MINIO_IGNORE_CERT_CHECK}"
    },
    "obm.minio.external.endpoint": {
      "sensitive": false,
      "value": "${MINIO_EXTERNAL_ENDPOINT}"
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
    },
    "kubernetes-secret-name": {
      "sensitive": false,
      "value": "eds-${DATA_PARTITION_ID}"
    },
    "indexer.service.account": {
      "sensitive": false,
      "value": "${INDEXER_SERVICE_ACCOUNT}"
    }
  }
}
EOF
}
