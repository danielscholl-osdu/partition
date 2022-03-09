## Service Configuration for Anthos

## Environment variables:

Define the following environment variables.

Must have:

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `SPRING_PROFILES_ACTIVE` | ex `anthos` | Spring profile that activate default configuration for Anthos environment | false | - |
| `OSM_POSTGRES_URL` | ex `jdbc:postgresql://127.0.0.1:5432/postgres` | Postgres server URL | no | - |
| `OSM_POSTGRES_USERNAME` | ex `postgres` | Postgres admin username | no | - |
| `OSM_POSTGRES_PASSWORD` | ex `postgres` | Postgres admin password | yes | - |

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


#### for OSM - Postgres:

### Schema configuration:

```
CREATE TABLE partition."PartitionProperty"(
id text COLLATE pg_catalog."default" NOT NULL,
pk bigint NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
data jsonb NOT NULL,
CONSTRAINT PartitionProperty_id UNIQUE (id)
);
CREATE INDEX PartitionProperty_dataGin ON partition."PartitionProperty" USING GIN (data);

```
