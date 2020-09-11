# Running Locally - Azure
## Requirements

In order to run this service locally, you will need the following:

- [Maven 3.6.0+](https://maven.apache.org/download.cgi)
- [Docker](https://www.docker.com/), to start a redis server locally

## General Tips

**Environment Variable Management**
The following tools make environment variable configuration simpler
 - [direnv](https://direnv.net/) - for a shell/terminal environment
 - [EnvFile](https://plugins.jetbrains.com/plugin/7861-envfile) - for [Intellij IDEA](https://www.jetbrains.com/idea/)

**Lombok**
This project uses [Lombok](https://projectlombok.org/) for code generation. You may need to configure your IDE to take advantage of this tool.
 - [Intellij configuration](https://projectlombok.org/setup/intellij)
 - [VSCode configuration](https://projectlombok.org/setup/vscode)
 
### Environment Variables

In order to run the service locally, you will need to have the following environment variables defined.

**Note** The following command can be useful to pull secrets from keyvault:
```bash
az keyvault secret show --vault-name $KEY_VAULT_NAME --name $KEY_VAULT_SECRET_NAME --query value -otsv
```

**Required to run service**

| name | value | description | sensitive? | source |
| ---  | ---   | ---         | ---        | ---    |
| `client-id` | `********` | AAD client application ID | yes | output of infrastructure deployment |
| `KEYVAULT_URI` | (non-secret) | KeyVault URI | no | variable `AZURE_KEYVAULT_URI` from GitLab variable group `Azure Target Env - {{env}}`
| `appinsights_key` | `********` | Application Insights Instrumentation Key, required to hook AppInsights with Partition application | yes | keyvault secret: `$KEYVAULT_URI/secrets/appinsights-key` |
| `AZURE_CLIENT_ID` | `********` | Identity to run the service locally. This enables access to Azure resources. You only need this if running locally | yes | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-username` |
| `AZURE_TENANT_ID` | `********` | AD tenant to authenticate users from | yes | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-tenant-id` |
| `AZURE_CLIENT_SECRET` | `********` | Secret for `$AZURE_CLIENT_ID` | yes | keyvault secret: `$KEYVAULT_URI/secrets/app-dev-sp-password` |

### Configure Maven

Check that maven is installed:
```bash
$ mvn --version
Apache Maven 3.6.0
Maven home: /usr/share/maven
Java version: 1.8.0_212, vendor: AdoptOpenJDK, runtime: /usr/lib/jvm/jdk8u212-b04/jre
...
```

You will need to configure access to the remote maven repository that holds the OSDU dependencies. This file should live within `~/.m2/settings.xml`:
```bash
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>os-core</id>
            <username>os-core</username>
            <password>${VSTS_FEED_TOKEN}</password>
        </server>
    </servers>
</settings>
```

### Start a Redis instance

Start a redis server locally with `docker run --name some-redis -p 6379:6379 -d redis` command

### Build and run the application

After configuring your environment as specified above, you can follow these steps to build and run the application. These steps should be invoked from the *repository root.*

```bash
# build + test + install core service code
$ mvn clean install

# build + test + package azure service code
$ mvn clean package -P partition-aks

# run service
#
# Note: this assumes that the environment variables for running the service as outlined
#       above are already exported in your environment.
$ cd provider/partition-azure && mvn spring-boot:run -f pom.xml
```

### Test the application

After the service has started it should be accessible via a web browser by visiting [http://localhost:8080/api/partition/v1/swagger-ui.html](http://localhost:8080/api/partition/v1/swagger-ui.html). If the request does not fail, you can then run the integration tests.

see [instructions](../../testing/README.md) on how to run integration tests locally. In addition to common testing environment variables, the `partition-test-azure` module also needs additional environment variables, which are described below:

 ```
    INTEGRATION_TESTER (app-dev-sp-username) 
    TESTER_SERVICEPRINCIPAL_SECRET (app-dev-sp-password)
    NO_DATA_ACCESS_TESTER (aad-no-data-access-tester-client-id)
    NO_DATA_ACCESS_TESTER_SERVICEPRINCIPAL_SECRET (aad-no-data-access-tester-secret)
    AZURE_AD_TENANT_ID (azure tenant id)
    AZURE_AD_APP_RESOURCE_ID (aad-client-id)
    AZURE_AD_OTHER_APP_RESOURCE_ID (AD Application ID used for negative testing)
 ```

A liveness check can also be performed at `http://localhost:8080/api/partition/v1/_ah/liveness_check`. Other apis can be found on the swagger page

## Debugging

Jet Brains - the authors of Intellij IDEA, have written an [excellent guide](https://www.jetbrains.com/help/idea/debugging-your-first-java-application.html) on how to debug java programs.

## License
Copyright 2017-2020, Schlumberger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at 

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.