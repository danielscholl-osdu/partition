#!/usr/bin/env bash

generate_post_data_gcp() {
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
      "value": "${PROJECT_ID}-${DATA_PARTITION_ID}-records"
    },
    "crmAccountID": {
      "sensitive": false,
      "value": "[${DATA_PARTITION_ID},${DATA_PARTITION_ID}]"
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
    }
  }
}
EOF
}
