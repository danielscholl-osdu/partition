## Service Configuration for GCP

## Environment variables:

Define the following environment variables.

Must have:

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `SPRING_PROFILES_ACTIVE` | ex `gcp` | Spring profile that activate default configuration for GCP environment | false | - |
| `PARTITION_ADMIN_ACCOUNTS` | ex `user` | Elasticsearch user, name of that variable not defined at the service level, the name will be received through partition service. Each tenant can have it's own ENV name value, and it must be present in ENV of Indexer service | yes | - |
| `GOOGLE_CLOUD_PROJECT` | ex `password` | Elasticsearch password, name of that variable not defined at the service level, the name will be received through partition service. Each tenant can have it's own ENV name value, and it must be present in ENV of Indexer service | false | - |
| `GOOGLE_AUDIENCES` | ex `*****.apps.googleusercontent.com` | Client ID for getting access to cloud resources | yes | https://console.cloud.google.com/apis/credentials |

Defined in default application property file but possible to override:

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `LOG_LEVEL` | `****` | Logging level | no | - |
| `LOG_PREFIX` | `service` | Logging prefix | no | - |
| `SERVER_SERVLET_CONTEXPATH` | `/api/partition/v1` | Servlet context path | no | - |
| `PARTITION_PROPERTY_KIND` | ex `PartitionProperty` | Kind name to store the properties. | no | - |
| `PARTITION_NAMESPACE` | ex `partition` | Namespace for database. | no | - |
| `SERVICE_ACCOUNT_TAIL` | `****` |By default Partition service while authenticating the request, verifies that the email in provided token belongs to a service account from a specific project by email tail `<GOOGLE_CLOUD_PROJECT> + .iam.gserviceaccount.com`, this behavior can be changed with this variable, you may specify which email tail exactly expected.| no | - |

These variables define service behavior, and are used to switch between `anthos` or `gcp` environments, their overriding and usage in mixed mode was not tested.
Usage of spring profiles is preferred.

| `OSMDRIVER` | ex `postgres` or `datastore` | Osm driver mode that defines which storage will be used | no | - |
| `ENVIRONMENT` | `gcp` or `anthos` | If `anthos` then authorization is disabled | no | - |

## Google cloud service account configuration :
TBD

| Required roles |
| ---    |
| - |