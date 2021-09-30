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

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.RetryLinearRetry;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.CloudTableClient;
import lombok.Setter;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.common.Validators;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import javax.inject.Named;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Configuration
@ConfigurationProperties(prefix = "azure.table-storage")
@Setter
public class TableStorageBootstrapConfig {

    private final static String CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net";

    private int maximumExecutionTimeMs;
    private int retryDeltaBackoffMs;
    private int retryMaxAttempts;

    @Bean
    @Lazy
    public CloudTableClient getCloudTableClient(
            final @Named("TABLE_STORAGE_ACCOUNT_NAME") String storageAccountName,
            final @Named("TABLE_STORAGE_ACCOUNT_KEY") String storageAccountKey) {
        try {
            Validators.checkNotNullAndNotEmpty(storageAccountName, "storageAccountName");
            Validators.checkNotNullAndNotEmpty(storageAccountKey, "storageAccountKey");

            final String storageConnectionString = String.format(CONNECTION_STRING, storageAccountName, storageAccountKey);
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
            CloudTableClient cloudTableClient = storageAccount.createCloudTableClient();
            cloudTableClient.getDefaultRequestOptions().setRetryPolicyFactory(new RetryLinearRetry(retryDeltaBackoffMs, retryMaxAttempts));
            cloudTableClient.getDefaultRequestOptions().setMaximumExecutionTimeInMs(maximumExecutionTimeMs);
            return cloudTableClient;
        } catch (URISyntaxException | InvalidKeyException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error creating cloud table storage client", e.getMessage(), e);
        }
    }

    @Bean
    @Lazy
    public CloudTable getCloudTable(
            final CloudTableClient cloudTableClient,
            final CloudTableConfiguration tblConfiguration) {
        try {
            Validators.checkNotNull(cloudTableClient, "cloudTableClient");
            Validators.checkNotNull(tblConfiguration, "tblConfiguration");

            CloudTable cloudTable = cloudTableClient.getTableReference(tblConfiguration.getCloudTableName());
            cloudTable.createIfNotExists();
            return cloudTable;
        } catch (URISyntaxException | StorageException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, String.format("Error querying cloud table: %s", tblConfiguration), e.getMessage(), e);
        }
    }
}
