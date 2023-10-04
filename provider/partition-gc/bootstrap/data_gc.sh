#!/usr/bin/env bash

# FIXME (GONRG-7695): Move elastic properties to additional partition when resolved
gc_system_partition_data() {
  DATA_PARTITION_ID_UPPER="${DATA_PARTITION_ID^^}"
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
    "bucket": {
      "sensitive": false,
      "value": "${PROJECT_ID}-${DATA_PARTITION_ID}-records"
    },
    "seismicBucket": {
      "sensitive": false,
      "value": "${PROJECT_ID}-${DATA_PARTITION_ID}-ss-seismic"
    },
    "crmAccountID": {
      "sensitive": false,
      "value": "[${DATA_PARTITION_ID},${DATA_PARTITION_ID}]"
    },
    "elasticsearch.host": {
      "sensitive": true,
      "value": "ELASTIC_HOST"
    },
    "elasticsearch.port": {
      "sensitive": true,
      "value": "ELASTIC_PORT"
    },
    "elasticsearch.user": {
      "sensitive": true,
      "value": "ELASTIC_USER"
    },
    "elasticsearch.password": {
      "sensitive": true,
      "value": "ELASTIC_PASS"
    }
  }
}
EOF
}

gc_additional_partition_data() {
  cat <<EOF
{
  "properties": {
    "policy-service-enabled": {
      "sensitive": false,
      "value": "false"
    },
    "kubernetes-secret-name": {
      "sensitive": false,
      "value": "eds-${DATA_PARTITION_ID}"
    },
    "index-augmenter-enabled": {
      "sensitive": false,
      "value": "${INDEXER_AUGMENTER_ENABLED}"
    },
    "reservoir-connection": {
      "sensitive": true,
      "value": "RESERVOIR_POSTGRES_CONN_STRING_OSDU"
    }
  }
}
EOF
}
