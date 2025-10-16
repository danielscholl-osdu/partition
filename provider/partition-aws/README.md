# Partition Service
The AWS Partition service is a [Spring Boot](https://spring.io/projects/spring-boot) service that creates, reads, updates, and destroys partition properties. The partition properties are stored in a [MongoDB database](https://www.mongodb.com/) and encrypted by [AWS KMS](https://aws.amazon.com/kms/) if the properties are flagged as sensitive.

## Running Locally
The following instructions are the minimum requirements for running the AWS partition service locally.

### Prerequisites
* [JDK 17]
* [Maven 3.8.0+](https://maven.apache.org/download.cgi)
* Lombok 1.18 or later
* IDE ([IntelliJ](https://www.jetbrains.com/idea/download/) is preferred)
* [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)
* [Postman](https://www.postman.com/)
* [MongoDB](https://docs.mongodb.com/v4.0/installation/) on local machine or a MongoDB connection string for test database.
* [Mongo Compass](https://www.mongodb.com/products/compass) <Optional>
* OSDU Instance deployed on AWS

### Service Configuration
The following environment variables need to be defined to run the service locally.

| Name | Example Value | Description | Sensitive? | Source |
| ---  | ---   | ---         | ---        | ---    |
| `LOCAL_MODE` | `true` | A required flag to indicate to the authorization service that partition service is running locally versus in the cluster | no | - |
| `AWS_REGION` | ex `us-east-1` | The region where resources needed by the service are deployed | no | - |
| `AWS_ACCESS_KEY_ID` | - | The AWS Access Key for a user with access to Backend Resources required by the service | yes | [temporary security credentials](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_temp_use-resources.html) |
| `AWS_SECRET_ACCESS_KEY` | - | The AWS Secret Key for a user with access to Backend Resources required by the service | yes | [temporary security credentials](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_temp_use-resources.html) |
| `AWS_SESSION_TOKEN` | - | AWS Session token needed if using an SSO user session to authenticate | yes | [temporary security credentials](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_credentials_temp_use-resources.html) |
| `ENVIRONMENT` | ex `osdu-dev` | The name of the environment stack name | no | Output by infrastructure deployment |
| `ENTITLEMENTS_BASE_URL` | ex `https://alias.dev.osdu.aws` | The base URL of OSDU | no | Output by infrastructure deployment |
| `MONGODB_ENDPOINT` | ex `mongodbname.robot.mongodb.net` or `localhost` | MongoDB database endpoint used for local development | yes | https://www.mongodb.com/ |
| `MONGODB_AUTH_DATABASE` | ex `partitions` | The database name within the MongoDB instance - defaults to `partitions` | no | - |
| `MONGODB_USERNAME` | ex `admin` | MongoDB username used as part of the connection string | yes | - |
| `MONGODB_PASSWORD` | - | MongoDB password used as part of the connection string | yes | - |
| `MONGODB_USE_SRV_ENDPOINT` | `false` or `true` | To run the service locally, set this to false | no | - |
| `KEY_ARN` | ex `arn:aws:kms:{AWS_REGION}:{AWS_ACCOUNT_ID}:key/1234abcd-12ab-34cd-56ef-1234567890ab` | A symmetric AWS KMS encryption key ARN with the appropriate key user policy that allows encrypt/decrypt access to the IAM role configured in the following step | yes | https://docs.aws.amazon.com/kms/latest/developerguide/create-keys.html |

### MongoDB Setup Locally
* Navigate to where the mongo sever is installed and start the server using:
  ``C:\Program Files\MongoDB\Server\5.0\bin>mongod``

The server will start on the default port 27017
* Launch MongoDB Compass and create a new connection to localhost: 27017
![New Connection](docs/img/newconn.png)

* Next add a db user using mongo shell using the following commands:
![Add new db user](docs/img/mongo_createuser.png)

### Run Locally
Check that maven is installed:

```bash
mvn --version
Apache Maven 3.8.3
Maven home: C:\opt\apache-maven-3.8.3
Java version: 17.0.7
```

You may need to configure access to the remote maven repository that holds the OSDU dependencies. Copy one of the below files' content to your .m2 folder
* For development against the OSDU GitLab environment, leverage: `<REPO_ROOT>~/.mvn/community-maven.settings.xml`
* For development in an AWS Environment, leverage: `<REPO_ROOT>/provider/legal-aws/maven/settings.xml`


* Navigate to the AWS partition service's root folder and run:
```bash
mvn clean package -pl partition-core,provider/partition-aws
```

* If you wish to build the project without running tests
```bash
mvn clean package -pl partition-core,provider/partition-aws -DskipTests
```

After configuring your environment as specified above, you can follow these steps to run the application. These steps should be invoked from the *repository root.*
NOTE: If not on osx/linux: Replace `*` with version numbers as defined in the provider/partition-aws/pom.xml file

```bash
java -jar provider/partition-aws/target/partition-aws-*.*.*-SNAPSHOT-spring-boot.jar
```

Alternatively, if using IntelliJ, you can configure your environment variables and run configuration by selecting Run>Edit Configurations and fill in the below information:
Main Class: org.opengroup.osdu.partition.provider.aws.PartitionApplication
Use Classpath of Module: partition-aws
Environment Variables: (As defined above)

To run the configuration, select Run>Run and select your configuration. 

### Hitting Partitions API
The service will be accessible at [http://localhost:8080/api/partition/v1/partitions/](http://localhost:8080/api/partition/v1/partitions/). A /info endpoint is available at [http://localhost:8080/api/partition/v1/info/](http://localhost:8080/api/partition/v1/info/).

A bearer access token is required to authorize all partitions API requests, except for /info. To generate an access token a POST request must be sent to the following URL:
{{auth_token_url}}?grant_type=client_credentials&client_id={{client_id}}&scope={{scope}}. The request must use "Basic Auth" with the client ID and secret passed in as parameters. The table below explains where to find these parameters:

| Parameter | Value | Sensitive? | Source |
| ---  | ---   | ---        | ---    |
| `auth_token_url` | ex `https://osdu-dev-888733619319.auth.us-east-1.amazoncognito.com/oauth2/token` | no | Found in AWS SSM under resource path /osdu/{resource_prefix}/client-credentials-client-id |
| `client_id` | - | yes | Found in AWS SSM under resource path /osdu/{resource_prefix}/client-credentials-client-id |
| `client_secret` | - | yes | Found in AWS Secrets Manager under resource path /osdu/{resource_prefix}/client_credentials_secret |
| `scope` | ex `osduOnAws/osduOnAWSService` | no | Found in AWS SSM under resource path /osdu/{resource_prefix}/oauth-custom-scope |

All partitions API requests should use Bearer Token auth using the access token returned from hitting the above endpoint.

## Testing
### Running Unit Tests
Navigate to the partition service's root folder:
```bash
cd provider/partition-aws
```
Install the project dependencies and run unit tests:
```bash
mvn clean install
```

### Running Integration Tests
Execute following command to build code and run all the integration tests from the root folder:

```bash
mvn clean package -f testing/pom.xml -pl partition-test-core,partition-test-aws -DskipTests
mvn test -f testing/partition-test-aws/pom.xml
```

## Licence
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
