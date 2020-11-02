## Partition Service

## Table of Contents <a name="TOC"></a>
* [Introduction](#introduction)
* [Checking Service Health](#checking-service-health)
* [Partition API access](#partition-api-access)
* [APIs](#apis)
    * [Get partition details](#get-partition)
    * [Create a new partition](#create-partition)
    * [Delete an existing partition](#delete-partition)
    * [List of partitions](#list-partition)
    

## Introduction <a name="introduction"></a>
Partition service is responsible for creating and retrieving the partition specific properties (secret and non-secret) on behalf of other services.

## Health Check <a name="checking-service-health"></a>
An endpoint to check if service is up and running.
```
GET api/partition/v1/_ah/liveness_check
```
<details><summary>curl</summary>

```
curl --request GET \
  --url 'https://<base_url>/api/partition/v1/_ah/liveness_check'
```
</details>

## Partition API access <a name="partition-api-access"></a>
As Partition service APIs are mostly consumed by other services, API access is limited to admins/service accounts only.

## APIs <a name="apis"></a>
### Get partition details<a name="get-partition"></a>
Consuming services can use this API to get details of a partition. Partition details consists of a set of key-value pairs of properties.
```
GET api/partition/v1/partitions/{partitionId}
```
<details><summary>curl</summary>

```
curl --request GET \
  --url 'https://<base_url>/api/partition/v1/partitions/common' \
  --header 'Authorization: Bearer <JWT>' \
  --header 'Content-Type: application/json'
```
</details>

A sample output is shown below.
<details><summary>Sample response</summary>

```
{
    "compliance-ruleset": {
        "sensitive": false,
        "value": "shared"
    },
    "elastic-endpoint": {
        "sensitive": true,
        "value": "common-elastic-endpoint"
    },
    "elastic-username": {
        "sensitive": true,
        "value": "common-elastic-username"
    },
    "elastic-password": {
        "sensitive": true,
        "value": "common-elastic-password"
    },
    "cosmos-connection": {
        "sensitive": true,
        "value": "common-cosmos-connection"
    },
    "cosmos-endpoint": {
        "sensitive": true,
        "value": "common-cosmos-endpoint"
    },
    "id": {
        "sensitive": false,
        "value": "common"
    }
}
```

</details>

[Back to Table of Contents](#TOC)

### Create a new partition<a name="create-partition"></a>
This api can be used to create a new partition. A plausible use case would be partition provisioning infrastructure script.
```
POST api/partition/v1/partitions/{partitionId}
```
<details><summary>curl</summary>

```
curl --request POST \
  --url 'https://<base_url>/api/partition/v1/partitions/mypartition' \
  --header 'Authorization: Bearer <JWT>' \
  --header 'Content-Type: application/json' \
  --data-raw '{
      "properties": {
          "compliance-ruleset": {
              "value": "shared"
          },
          "elastic-endpoint": {
              "sensitive": true,
              "value": "elastic-endpoint"
          },
          "elastic-username": {
              "sensitive": true,
              "value": "elastic-username"
          },
          "elastic-password": {
              "sensitive": true,
              "value": "elastic-password"
          },
          "cosmos-connection": {
              "sensitive": true,
              "value": "cosmos-connection"
          },
          "cosmos-endpoint": {
              "sensitive": true,
              "value": "cosmos-endpoint"
          }
      }
  }'
```
</details>

### Delete an existing partition<a name="delete-partition"></a>
This api is used to delete an existing partition. A plausible use case would be partition teardown infrastructure script.
```
DELETE api/partition/v1/partitions/{partitionId}
```
<details><summary>curl</summary>

```
curl --request DELETE \
  --url 'https://<base_url>/api/partition/v1/partitions/mypartition' \
  --header 'Authorization: Bearer <JWT>' \
  --header 'Content-Type: application/json'
```
</details>


### List partitions <a name="list-partition"></a>
Consuming services can use this API to list all partitions Id.  
```
GET api/partition/v1/partitions
```
<details><summary>curl</summary>

```
curl --request GET \
  --url 'https://<base_url>/api/partition/v1/partitions' \
  --header 'Authorization: Bearer <JWT>' \
  --header 'Content-Type: application/json'
```
</details>

A sample output is shown below.
<details><summary>Sample response</summary>

```
[
    "default-dev",
    "opendes"
]
```

</details>

[Back to Table of Contents](#TOC)