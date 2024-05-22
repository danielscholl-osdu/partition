# Service Configuration for Baremetal

## Environment variables

Define the following environment variables.

Must have:

| name                     | value                                          | description                                                                  | sensitive? | source |
|--------------------------|------------------------------------------------|------------------------------------------------------------------------------|------------|--------|
| `SPRING_PROFILES_ACTIVE` | ex `anthos`                                    | Spring profile that activate default configuration for Baremetal environment | false      | -      |
| `OSM_POSTGRES_URL`       | ex `jdbc:postgresql://127.0.0.1:5432/postgres` | Postgres server URL                                                          | no         | -      |
| `OSM_POSTGRES_USERNAME`  | ex `postgres`                                  | Postgres admin username                                                      | no         | -      |
| `OSM_POSTGRES_PASSWORD`  | ex `postgres`                                  | Postgres admin password                                                      | yes        | -      |

Defined in default application property file but possible to override:

| name                        | value                  | description                        | sensitive? | source |
|-----------------------------|------------------------|------------------------------------|------------|--------|
| `LOG_LEVEL`                 | `****`                 | Logging level                      | no         | -      |
| `LOG_PREFIX`                | `service`              | Logging prefix                     | no         | -      |
| `SERVER_SERVLET_CONTEXPATH` | `/api/partition/v1`    | Servlet context path               | no         | -      |
| `PARTITION_PROPERTY_KIND`   | ex `PartitionProperty` | Kind name to store the properties. | no         | -      |
| `PARTITION_NAMESPACE`       | ex `partition`         | Namespace for database.            | no         | -      |
| `PARTITION_NAMESPACE`       | ex `partition`         | Namespace for database.            | no         | -      |
| `SYSTEM_PARTITION_ID`       | ex `syspartition`      | Name of system partition.          | yes        | -      |
| `SYSTEM_TENANT_API`         | ex `true`              | Enables system API                 | no         | -      |

These variables define service behavior, and are used to switch between `baremetal` or `gcp` environments, their overriding and usage in mixed mode was not tested.
Usage of spring profiles is preferred.

| `OSMDRIVER` | ex `postgres` or `datastore` | Osm driver mode that defines which storage will be used | no | - |
| `ENVIRONMENT` | `gcp` or `anthos` | If `anthos` then authorization is disabled | no | - |

### for OSM - Postgres

### Schema configuration

```sql
CREATE TABLE partition."PartitionProperty"(
id text COLLATE pg_catalog."default" NOT NULL,
pk bigint NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
data jsonb NOT NULL,
CONSTRAINT PartitionProperty_id UNIQUE (id)
);
CREATE INDEX PartitionProperty_dataGin ON partition."PartitionProperty" USING GIN (data);

```

### Running E2E Tests

You will need to have the following environment variables defined.

| name                                           | value                                   | description                                   | sensitive?                                        | source |
|------------------------------------------------|-----------------------------------------|-----------------------------------------------|---------------------------------------------------|--------|
| `ENVIRONMENT`                                  | ex `dev`                                |                                               | no                                                |        |
| `PARTITION_BASE_URL`                           | ex `http://localhost:8080/`             | service base URL                              | yes                                               |        |
| `CLIENT_TENANT`                                | ex `opendes`                            | name of the client partition                  | yes                                               |        |
| `MY_TENANT`                                    | ex `opendes`                            | name of the OSDU partition                    | yes                                               |        |
| `TEST_OPENID_PROVIDER_CLIENT_ID`               | `********`                              | Client Id for `$INTEGRATION_TESTER`           | yes                                               | --     |
| `TEST_OPENID_PROVIDER_CLIENT_SECRET`           | `********`                              |                                               | Client secret for `$INTEGRATION_TESTER`           | --     |
| `TEST_NO_ACCESS_OPENID_PROVIDER_CLIENT_ID`     | `********`                              | Client Id for `$NO_ACCESS_INTEGRATION_TESTER` | yes                                               | --     |
| `TEST_NO_ACCESS_OPENID_PROVIDER_CLIENT_SECRET` | `********`                              |                                               | Client secret for `$NO_ACCESS_INTEGRATION_TESTER` | --     |
| `TEST_OPENID_PROVIDER_URL`                     | `https://keycloak.com/auth/realms/osdu` | OpenID provider url                           | yes                                               | --     |

Execute following command to build code and run all the integration tests:

```bash
# Note: this assumes that the environment variables for integration tests as outlined
#       above are already exported in your environment.
$ (cd testing/partition-test-baremetal/ && mvn clean test)
```

## License

Copyright © Google LLC
Copyright © EPAM Systems

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
