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

import com.azure.security.keyvault.secrets.SecretClient;
import org.opengroup.osdu.azure.KeyVaultFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.inject.Named;

@Configuration
public class AzureBootstrapConfig {

    @Value("${azure.keyvault.url}")
    private String keyVaultURL;

    @Bean
    @Named("KEY_VAULT_URL")
    public String keyVaultURL() {
        return keyVaultURL;
    }

    @Bean
    @Named("TABLE_STORAGE_ACCOUNT_NAME")
    public String storageAccountName(SecretClient kv) {
        return KeyVaultFacade.getSecretWithValidation(kv, "tbl-storage");
    }

    @Bean
    @Named("TABLE_STORAGE_ACCOUNT_KEY")
    public String storageAccountKey(SecretClient kv) {
        return KeyVaultFacade.getSecretWithValidation(kv, "tbl-storage-key");
    }

    @Bean
    @Named("TABLE_STORAGE_ACCOUNT_ENDPOINT")
    public String storageAccountEndpoint(SecretClient kv) {
        return KeyVaultFacade.getSecretWithValidation(kv, "tbl-storage-endpoint");
    }

}
