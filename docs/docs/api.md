# Partition Service API

## Open API 3.0 - Swagger
- Swagger UI : https://host/context-path/swagger (will redirect to https://host/context-path/swagger-ui/index.html)
- api-docs (JSON) : https://host/context-path/api-docs
- api-docs (YAML) : https://host/context-path/api-docs.yaml

All the Swagger and OpenAPI related common properties are managed here [swagger.properties](https://community.opengroup.org/osdu/platform/system/partition/-/blob/master/partition-core/src/main/resources/swagger.properties)

## API Details

### Health Check
An endpoint to check if service is up and running.
```
GET api/partition/v1/actuator/health
```
<details><summary>Curl Get health</summary>

```
curl --request GET \
  --url 'https://<base_url>/api/partition/v1/actuator/health'
```
</details>


### Partition API access
As Partition service APIs are mostly consumed by other services, API access is limited to admins/service accounts only.

#### Get partition details
Consuming services can use this API to get details of a partition. Partition details consists of a set of key-value pairs of properties.
```
GET api/partition/v1/partitions/{partitionId}
```
<details><summary>Curl Get partition</summary>

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

#### Create a new partition
This api can be used to create a new partition. A plausible use case would be partition provisioning infrastructure script.
```
POST api/partition/v1/partitions/{partitionId}
```
<details><summary>Curl Create Partition</summary>

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

#### Update an existing partition
This api is used to update the properties of an existing partition. With this api, we can modify existing properties or add new ones. Deletion of properties can not be achieved, we'll have to delete the partition and re-create it for the same effect.
```
PATCH api/partition/v1/partitions/{partitionId}
```
<details><summary>Curl Patch Partition</summary>

```
curl --request PATCH \
  --url 'https://<base_url>/api/partition/v1/partitions/mypartition' \
  --header 'Authorization: Bearer <JWT>' \
  --header 'Content-Type: application/json' \
  --data-raw '{
      "properties": {
          "compliance-ruleset": {
              "value": "shared-update-value"
          },
          "new-key": {
              "sensitive": true,
              "value": "new-value"
          }
      }
  }'
```
</details>

#### Delete an existing partition
This api is used to delete an existing partition. A plausible use case would be partition teardown infrastructure script.
```
DELETE api/partition/v1/partitions/{partitionId}
```
<details><summary>Curl Delete partition</summary>

```
curl --request DELETE \
  --url 'https://<base_url>/api/partition/v1/partitions/mypartition' \
  --header 'Authorization: Bearer <JWT>' \
  --header 'Content-Type: application/json'
```
</details>


#### List partitions
Consuming services can use this API to list all partitions Id.  
```
GET api/partition/v1/partitions
```
<details><summary>Curl Get Partitions</summary>

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

## Version info endpoint
For deployment available public `/info` endpoint, which provides build and git related information.

#### Example response:
```json
{
    "groupId": "org.opengroup.osdu",
    "artifactId": "storage-gcp",
    "version": "0.10.0-SNAPSHOT",
    "buildTime": "2021-07-09T14:29:51.584Z",
    "branch": "feature/GONRG-2681_Build_info",
    "commitId": "7777",
    "commitMessage": "Added copyright to version info properties file",
    "connectedOuterServices": [
      {
        "name": "elasticSearch",
        "version":"..."
      },
      {
        "name": "postgresSql",
        "version":"..."
      },
      {
        "name": "redis",
        "version":"..."
      }
    ]
}
```

This endpoint takes information from files, generated by `spring-boot-maven-plugin`,
`git-commit-id-plugin` plugins. Need to specify paths for generated files to matching
properties:

- `version.info.buildPropertiesPath`
- `version.info.gitPropertiesPath`