# Partition Service
os-partition-reference is a [Spring Boot](https://spring.io/projects/spring-boot) service that is responsible for creating and retrieving partition specific properties on behalf of other services whether they are secret values or not.

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites
Pre-requisites

* GCloud SDK with java (latest version)
* JDK 8
* Lombok 1.16 or later
* Maven

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
| `MONGO_DB_URL` | ex `localhost:27017` | Mongo DB Url| yes | output of infrastructure deployment |
| `MONGO_DB_USER` | ex `mongouser` | Mongo DB userName| yes | output of infrastructure deployment |
| `MONGO_DB_PASSWORD` | ex `mongopassword` | Mongo DB userPassword. If password contains `@` symbols, it must be encoded to URL-encoded format. | yes | output of infrastructure deployment |
| `MONGO_DB_NAME` | ex `mongoDBName` | Mongo DB DbName| yes | output of infrastructure deployment |

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
cd provider/partition-reference/ && mvn spring-boot:run
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
GKE Google Documentation: https://cloud.google.com/build/docs/deploying-builds/deploy-gke
Anthos Google Documentation: https://cloud.google.com/anthos/multicluster-management/gateway/tutorials/cloud-build-integration

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
