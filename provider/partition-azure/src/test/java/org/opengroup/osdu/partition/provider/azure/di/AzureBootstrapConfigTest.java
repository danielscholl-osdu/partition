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
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AzureBootstrapConfigTest {

    private final AzureBootstrapConfig bootstrapConfig = new AzureBootstrapConfig();

    private final SecretClient kv = mock(SecretClient.class);

    @Test
    public void config_returnsCorrectSecret_tblStorage() {
        KeyVaultSecret secret = mock(KeyVaultSecret.class);
        doReturn("tbl-storage").when(secret).getValue();
        doReturn(secret).when(kv).getSecret("tbl-storage");

        String secretValue = bootstrapConfig.storageAccountName(kv);
        assertEquals("tbl-storage", secretValue, "Secret value was incorrect");
    }
}
