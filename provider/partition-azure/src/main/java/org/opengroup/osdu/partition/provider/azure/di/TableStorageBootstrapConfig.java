// Copyright 2017-2020, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.partition.provider.azure.di;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableServiceClient;
import com.azure.data.tables.TableServiceClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import lombok.Setter;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.azure.di.PodIdentityConfiguration;
import org.opengroup.osdu.azure.di.WorkloadIdentityConfiguration;
import org.opengroup.osdu.common.Validators;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import jakarta.inject.Named;
import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "azure.table-storage")
@Setter
public class TableStorageBootstrapConfig {

    private int maximumExecutionTimeMs;
    private int retryDeltaBackoffMs;
    private int retryMaxAttempts;

    @Autowired
    private PodIdentityConfiguration podIdentityConfiguration;

    @Autowired
    private WorkloadIdentityConfiguration workloadIdentityConfiguration;

    private final static String CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net";

    @Bean
    @Lazy
    public TableServiceClient getTableServiceClient(
            final @Named("TABLE_STORAGE_ACCOUNT_NAME") String storageAccountName,
            final @Named("TABLE_STORAGE_ACCOUNT_KEY") String storageAccountKey,
            final @Named(value = "TABLE_STORAGE_ACCOUNT_ENDPOINT") String storageAccountEndpoint) {
        try {
            // Set up retry options first for all authentication methods
            FixedDelayOptions fixedDelayOptions = new FixedDelayOptions(retryMaxAttempts, Duration.ofMillis(retryDeltaBackoffMs));
            RetryOptions retryOptions = new RetryOptions(fixedDelayOptions);
            
            TableServiceClientBuilder builder = new TableServiceClientBuilder().retryOptions(retryOptions);
            
            if (Boolean.TRUE.equals(podIdentityConfiguration.getIsEnabled()) &&
                Boolean.TRUE.equals(workloadIdentityConfiguration.getIsEnabled())) {
                // Use managed identity authentication with DefaultAzureCredential  
                Validators.checkNotNullAndNotEmpty(storageAccountEndpoint, "storageAccountEndpoint");
                
                TokenCredential credential = new DefaultAzureCredentialBuilder().build();
                builder.endpoint(storageAccountEndpoint).credential(credential);  
            } else {
                // Use connection string-based authentication (original method)
                Validators.checkNotNullAndNotEmpty(storageAccountName, "storageAccountName");
                Validators.checkNotNullAndNotEmpty(storageAccountKey, "storageAccountKey");

                final String storageConnectionString = String.format(CONNECTION_STRING, storageAccountName, storageAccountKey);
                builder.connectionString(storageConnectionString);
            }

            return builder.buildClient();
        }
        catch (Exception e){
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error creating cloud table storage client", e.getMessage(), e);
        }
    }

    @Bean
    @Lazy
    public TableClient getTableClient(
            TableServiceClient tableServiceClient,
            final DataTableConfiguration tblConfiguration) {
        try {
            Validators.checkNotNull(tableServiceClient, "tableServiceClient");
            Validators.checkNotNull(tblConfiguration, "tblConfiguration");

            //Attempting to create the table first, since if the table is already existing we get a null tableClient.
            //The behaviour of the API when a table is existing was not clearly documented here
            //https://learn.microsoft.com/en-us/java/api/com.azure.data.tables.tableserviceclient?view=azure-java-stable#com-azure-data-tables-tableserviceclient-createtableifnotexists(java-lang-string).
            TableClient tableClient = tableServiceClient.createTableIfNotExists(tblConfiguration.getCloudTableName());
            if(tableClient == null){
                //On the other hand, if we attempt to getTableClient for a non-existent table, it would not give us null
                tableClient = tableServiceClient.getTableClient(tblConfiguration.getCloudTableName());
            }
            return tableClient;
        }
        catch (Exception e){
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, String.format("Error creating a Table Client for table: %s", tblConfiguration), e.getMessage(), e);
        }

    }
}
