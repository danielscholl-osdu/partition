# Partition Service
os-partition-gcp is a [Spring Boot](https://spring.io/projects/spring-boot) service that is responsible for creating and retrieving partition specific properties on behalf of other services whether they are secret values or not.

## Features of implementation
This is a universal solution created using EPAM OSM mapper technology. It allows you to work with various
implementations of KV stores.

## Limitations of the current version
In the current version, the mappers have been equipped with several drivers to the stores:

OSM (mapper for KV-data): Google Datastore; Postgres


## Extensibility
To use any other store or message broker, implement a driver for it. With an extensible set of drivers, the solution is
unrestrictedly universal and portable without modification to the main code.
Mappers support "multitenancy" with flexibility in how it is implemented. They switch between datasources of different
tenants due to the work of a bunch of classes that implement the following interfaces:

* Destination - takes a description of the current context, e.g., "data-partition-id = opendes";
* DestinationResolver – accepts Destination, finds the resource, connects, and returns Resolution;
* DestinationResolution – contains a ready-made connection, the mapper uses it to get the data.


## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites
Pre-requisites

* GCloud SDK with java (latest version)
* JDK 8
* Lombok 1.16 or later
* Maven

## Mapper tuning mechanisms

This service uses specific implementations of DestinationResolvers. A total of 2 resolvers have been implemented, which are divided into two groups:

### for universal technologies:

- for Postgres: osm/config/resolver/OsmPostgresDestinationResolver.java

#### Their algorithms are as follows:

- incoming Destination carries data-partition-id
- resolver accesses the Partition service and gets PartitionInfo
- from PartitionInfo resolver retrieves properties for the connection: URL, username, password etc.
- resolver creates a data source, connects to the resource, remembers the datasource
- resolver gives the datasource to the mapper in the Resolution object

### for native Google Cloud technologies:

- for Datastore: osm/config/resolver/OsmDatastoreDestinationResolver.java

#### Their algorithms are similar,

Except that they do not receive special properties from the Partition service for connection, because the location of
the resources is unambiguously known - they are in the GCP project. And credentials are also not needed - access to data
is made on behalf of the Google Identity SA under which the service itself is launched. Therefore, resolver takes only
the value of the **projectId** property from PartitionInfo and uses it to connect to a resource in the corresponding GCP
project.

### Installation
In order to run the service locally or remotely, you will need to have the following environment variables defined.

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `LOG_PREFIX` | `service` | Logging prefix | no | - |
| `SERVER_SERVLET_CONTEXPATH` | `/api/partition/v1` | Servlet context path | no | - |
| `AUTHORIZE_API` | ex `https://entitlements.com/entitlements/v1` | Entitlements API endpoint | no | output of infrastructure deployment |
| `GOOGLE_CLOUD_PROJECT` | ex `osdu-cicd-epam` | Google Cloud Project Id| no | output of infrastructure deployment |
| `GOOGLE_AUDIENCES` | ex `*****.apps.googleusercontent.com` | Client ID for getting access to cloud resources | yes | https://console.cloud.google.com/apis/credentials |
| `PARTITION_ADMIN_ACCOUNTS` | ex `admin@domen.iam.gserviceaccount.com,osdu-gcp-sa,workload-identity` | List of partition admin account emails, could be in full form like `admin@domen.iam.gserviceaccount.com` or in `starts with` pattern like `osdu-gcp-sa`| no | - |
| `GOOGLE_APPLICATION_CREDENTIALS` | ex `/path/to/directory/service-key.json` | Service account credentials, you only need this if running locally | yes | https://console.cloud.google.com/iam-admin/serviceaccounts |
| `KEY_RING` | ex `csqp` | A key ring holds keys in a specific Google Cloud location and permit us to manage access control on groups of keys | yes | https://cloud.google.com/kms/docs/resource-hierarchy#key_rings |
| `KMS_KEY` | ex `partitionService` | A key exists on one key ring linked to a specific location. | yes | https://cloud.google.com/kms/docs/resource-hierarchy#key_rings |
| `PARTITION_PROPERTY_KIND` | ex `PartitionProperty` | Kind name to store the properties. | no | - |
| `PARTITION_NAMESPACE` | ex `partition` | Namespace for database. | no | - |
| `osmDriver` | ex `postgres` or `datastore` | Osm driver mode that defines which storage will be used | no | - |
| `osm.postgres.url` | ex `jdbc:postgresql://127.0.0.1:5432/postgres` | Postgres server URL | no | - |
| `osm.postgres.username` | ex `postgres` | Postgres admin username | no | - |
| `osm.postgres.password` | ex `postgres` | Postgres admin password | yes | - |

## Configuring mappers' Datasources

When using non-Google-Cloud-native technologies, property sets must be defined on the Partition service as part of
PartitionInfo for each Tenant.

They are specific to each storage technology:

#### for OSM - Postgres:

**database structure**
OSM works with data logically organized as "partition"->"namespace"->"kind"->"record"->"columns". The above sequence
describes how it is named in Google Datastore, where "partition" maps to "GCP project".

This is how **Postgres** OSM driver does. Notice, the above hierarchy has been kept, but Postgres uses alternative entities
for it.

| Datastore hierarchy level |     | Postgres alternative used  |
|---------------------------|-----|----------------------------|
| partition (GCP project)   | ==  | Postgres server URL        |
| namespace                 | ==  | Schema                     |
| kind                      | ==  | Table                      |
| record                    | ==  | '<multiple table records>' |
| columns                   | ==  | id, data (jsonb)           |

As we can see in the above table, Postgres uses different approach in storing business data in records. Not like
Datastore, which segments data into multiple physical columns, Postgres organises them into the single JSONB "data"
column. It allows provisioning new data registers easily not taking care about specifics of certain registers structure.
In the current OSM version (as on December'21) the Postgres OSM driver is not able to create new tables in runtime.

So this is a responsibility of DevOps / CICD to provision all required SQL tables (for all required data kinds) when on new
environment or tenant provisioning when using Postgres. Detailed instructions (with examples) for creating new tables is
in the **OSM module Postgres driver README.md** `org/opengroup/osdu/core/gcp/osm/translate/postgresql/README.md`

As a quick shortcut, this example snippet can be used by DevOps DBA:

* `exampleschema` equals to `PARTITION_NAMESPACE` 
* `ExampleKind` equals to `PARTITION_PROPERTY_KIND`

```postgres-psql
--CREATE SCHEMA "<exampleschema>";
CREATE TABLE <exampleschema>."<ExampleKind>"(
    id text COLLATE pg_catalog."default" NOT NULL,
    pk bigint NOT NULL GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    data jsonb NOT NULL,
    CONSTRAINT <ExampleKind>_id UNIQUE (id)
);
CREATE INDEX <ExampleKind>_datagin ON <exampleschema>."<ExampleKind>" USING GIN (data);
```

### Run Locally
Check that maven is installed:

```bash
$ mvn --version
Apache Maven 3.6.0
Maven home: /usr/share/maven
Java version: 1.8.0_212, vendor: AdoptOpenJDK, runtime: /usr/lib/jvm/jdk8u212-b04/jre
...
```

You may need to configure access to the remote maven repository that holds the OSDU dependencies. This file should live within `~/.mvn/community-maven.settings.xml`:

```bash
$ cat ~/.m2/settings.xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>community-maven-via-private-token</id>
            <!-- Treat this auth token like a password. Do not share it with anyone, including Microsoft support. -->
            <!-- The generated token expires on or before 11/14/2019 -->
             <configuration>
              <httpHeaders>
                  <property>
                      <name>Private-Token</name>
                      <value>${env.COMMUNITY_MAVEN_TOKEN}</value>
                  </property>
              </httpHeaders>
             </configuration>
        </server>
    </servers>
</settings>
```
* Update the Google cloud SDK to the latest version:

```bash
gcloud components update
```
* Set Google Project Id:

```bash
gcloud config set project <YOUR-PROJECT-ID>
```

* Perform a basic authentication in the selected project:

```bash
gcloud auth application-default login
```

* Navigate to partition service's root folder and run:

```bash
mvn clean install   
```

* If you wish to see the coverage report then go to target/site/jacoco/index.html and open index.html

* If you wish to build the project without running tests

```bash
mvn clean install -DskipTests
```

After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the *repository root.*

```bash
cd provider/partition-gcp/ && mvn spring-boot:run
```

## Testing
Navigate to partition service's root folder and run all the tests:

```bash
# build + install integration test core
$ (cd testing/partition-test-core/ && mvn clean install)
```

### Running E2E Tests
This section describes how to run cloud OSDU E2E tests (testing/partition-test-gcp).

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

## Deployment
Partition Service is compatible with App Engine Flexible Environment and Cloud Run.

* To deploy into Cloud run, please, use this documentation:
  https://cloud.google.com/run/docs/quickstarts/build-and-deploy

* To deploy into App Engine, please, use this documentation:
  https://cloud.google.com/appengine/docs/flexible/java/quickstart

#### Cloud KMS Setup

Enable cloud KMS on master project.

Create king ring and key in the ***master project***

```bash
    gcloud services enable cloudkms.googleapis.com
    export KEYRING_NAME="csqp"
    export CRYPTOKEY_NAME="partionService"
    gcloud kms keyrings create $KEYRING_NAME --location global
    gcloud kms keys create $CRYPTOKEY_NAME --location global \
    		--keyring $KEYRING_NAME \
    		--purpose encryption
```

Add **Cloud KMS CryptoKey Encrypter/Decrypter** role to the used **service account** by Partition Service of the ***master project*** through IAM - Role tab.

Add "Cloud KMS Encrypt/Decrypt" role to the used **service account** by Partition Service of the ***master project*** through IAM - Role tab.

## Licence
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
