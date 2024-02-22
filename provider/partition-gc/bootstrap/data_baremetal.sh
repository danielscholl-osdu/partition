#!/usr/bin/env bash

# FIXME (GONRG-7695): Move elastic properties to additional partition when resolved
# FIXME (GONRG-7696): Move rabbitmq properties to additional partition when resolved
baremetal_system_partition_data() {
  DATA_PARTITION_ID_UPPER="${DATA_PARTITION_ID_VALUE^^}"
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
      "value": "${DATA_PARTITION_ID_VALUE}"
    },
    "name": {
      "sensitive": false,
      "value": "${DATA_PARTITION_ID_VALUE}"
    },
    "bucket": {
      "sensitive": false,
      "value": "${BUCKET_PREFIX}-${DATA_PARTITION_ID_VALUE}-records"
    },
    "crmAccountID": {
      "sensitive": false,
      "value": "[${DATA_PARTITION_ID_VALUE},${DATA_PARTITION_ID_VALUE}]"
    },
    "osm.postgres.datasource.url": {
      "sensitive": true,
      "value": "POSTGRES_DATASOURCE_URL${PARTITION_SUFFIX}"
    },
    "osm.postgres.datasource.username": {
      "sensitive": true,
      "value": "POSTGRES_DB_USERNAME${PARTITION_SUFFIX}"
    },
    "osm.postgres.datasource.password": {
      "sensitive": true,
      "value": "POSTGRES_DB_PASSWORD${PARTITION_SUFFIX}"
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
    "obm.minio.ui.endpoint": {
        "sensitive": false,
        "value": "${MINIO_UI_ENDPOINT}"
    },
    "kubernetes-secret-name": {
      "sensitive": false,
      "value": "eds-${DATA_PARTITION_ID_VALUE}"
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
      "value": "ELASTIC_HOST${PARTITION_SUFFIX}"
    },
    "elasticsearch.port": {
      "sensitive": true,
      "value": "ELASTIC_PORT${PARTITION_SUFFIX}"
    },
    "elasticsearch.user": {
      "sensitive": true,
      "value": "ELASTIC_USER${PARTITION_SUFFIX}"
    },
    "elasticsearch.password": {
      "sensitive": true,
      "value": "ELASTIC_PASS${PARTITION_SUFFIX}"
    },
    "index-augmenter-enabled": {
      "sensitive": false,
      "value": "${INDEXER_AUGMENTER_ENABLED}"
    },
    "policy-service-enabled": {
      "sensitive": false,
      "value": "false"
    },
    "obm.minio.external.endpoint": {
      "sensitive": false,
      "value": "${MINIO_EXTERNAL_ENDPOINT}"
    },
    "entitlements.datasource.url": {
      "sensitive": true,
      "value": "ENT_PG_URL${PARTITION_SUFFIX}"
    },
    "entitlements.datasource.username": {
      "sensitive": true,
      "value": "ENT_PG_USER${PARTITION_SUFFIX}"
    },
    "entitlements.datasource.password": {
      "sensitive": true,
      "value": "ENT_PG_PASS${PARTITION_SUFFIX}"
    },
    "entitlements.datasource.schema": {
      "sensitive": true,
      "value": "ENT_PG_SCHEMA_${DATA_PARTITION_ID_UPPER}"
    }
  }
}
EOF
}

baremetal_additional_partition_data() {
  DATA_PARTITION_ID_UPPER="${DATA_PARTITION_ID_VALUE^^}"
  cat <<EOF
{
  "properties": {
    "index-augmenter-enabled": {
      "sensitive": false,
      "value": "${INDEXER_AUGMENTER_ENABLED}"
    },
    "policy-service-enabled": {
      "sensitive": false,
      "value": "false"
    },
    "obm.minio.external.endpoint": {
      "sensitive": false,
      "value": "${MINIO_EXTERNAL_ENDPOINT}"
    }
  }
}
EOF
}
