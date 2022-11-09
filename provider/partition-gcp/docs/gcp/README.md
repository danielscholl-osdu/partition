## Service Configuration for Google Cloud

## Environment variables:

Define the following environment variables.

Must have:

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `SPRING_PROFILES_ACTIVE` | ex `gcp` | Spring profile that activate default configuration for Google Cloud environment | false | - |
| `PARTITION_ADMIN_ACCOUNTS` | ex `user` | Elasticsearch user, name of that variable not defined at the service level, the name will be received through partition service. Each tenant can have it's own ENV name value, and it must be present in ENV of Indexer service | yes | - |
| `GOOGLE_CLOUD_PROJECT` | ex `password` | Elasticsearch password, name of that variable not defined at the service level, the name will be received through partition service. Each tenant can have it's own ENV name value, and it must be present in ENV of Indexer service | false | - |

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

## Google Cloud service account configuration :
TBD

| Required roles |
| ---    |
| - |

### Running E2E Tests

You will need to have the following environment variables defined.

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `ENVIRONMENT` | ex `dev` |  | no |  |
| `PARTITION_BASE_URL` | ex `http://localhost:8080/` | service base URL | yes |  |
| `CLIENT_TENANT` | ex `opendes` | name of the client partition | yes |  |
| `MY_TENANT` | ex `opendes` | name of the OSDU partition | yes |  |
| `INTEGRATION_TESTER` | `********` | Service account for API calls. Note: this user must be `PARTITION_ADMIN_ACCOUNT` | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |
| `NO_DATA_ACCESS_TESTER` | `********` | Service account base64 encoded string without data access | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |
| `INTEGRATION_TEST_AUDIENCE` | `********` | client application ID | yes | https://console.cloud.google.com/apis/credentials |

Execute following command to build code and run all the integration tests:

```bash
# Note: this assumes that the environment variables for integration tests as outlined
#       above are already exported in your environment.
$ (cd testing/partition-test-gcp/ && mvn clean test)
```
