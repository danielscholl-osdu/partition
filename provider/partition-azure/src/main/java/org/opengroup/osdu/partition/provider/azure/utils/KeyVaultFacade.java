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

package org.opengroup.osdu.partition.provider.azure.utils;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.common.Validators;
import org.opengroup.osdu.core.common.model.http.AppException;

import java.util.ArrayList;
import java.util.List;

public final class KeyVaultFacade {

    /**
     * Checks if the secret exists in KV
     *
     * @param secretClient secret client
     * @param secretName   name of secret
     * @return True if secrete exist
     */
    public static boolean secretExists(SecretClient secretClient, String secretName) {
        Validators.checkNotNull(secretClient, "secretClient can't be null");
        Validators.checkNotNullAndNotEmpty(secretName, "secretName can't null or empty");

        try {
            secretClient.getSecret(secretName);
        } catch (ResourceNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * Gets all secret from KV starting with prefix.
     *
     * @param secretClient secret client
     * @param prefix       secrete key prefix
     * @return List of secret keys.
     */
    public static List<String> getKeyVaultSecrets(SecretClient secretClient, String prefix) {
        Validators.checkNotNull(secretClient, "secretClient can't be null");
        Validators.checkNotNullAndNotEmpty(prefix, "prefix can't null or empty");

        List<String> out = new ArrayList<>();
        PagedIterable<SecretProperties> secretProperties = secretClient.listPropertiesOfSecrets();
        for (PagedResponse<SecretProperties> resp : secretProperties.iterableByPage()) {
            resp.getItems().stream().filter(value -> value.getName().startsWith(prefix)).map(SecretProperties::getName).forEach(out::add);
        }

        return out;
    }

    /**
     * Gets a secret from KV and validates that it is not null or empty.
     *
     * @param secretClient secret client
     * @param secretName   name of secret
     * @return Secret value. This is guaranteed to be not null or empty.
     */
    public static String getKeyVaultSecret(SecretClient secretClient, String secretName) {
        Validators.checkNotNull(secretClient, "secretClient can't be null");
        Validators.checkNotNullAndNotEmpty(secretName, "secretName can't null or empty");
        KeyVaultSecret secret;

        try {
            secret = secretClient.getSecret(secretName);
        } catch (ResourceNotFoundException e) {
            throw new AppException(HttpStatus.SC_NOT_FOUND, "partition not found", String.format("%s partition not found", secretName));
        }

        if (secret == null) {
            throw new IllegalStateException(String.format("No secret found with name %s", secretName));
        }

        String secretValue = secret.getValue();
        if (secretValue == null) {
            throw new IllegalStateException(String.format(
                    "Secret unexpectedly missing from KeyVault response for secret with name %s", secretName));
        }

        return secretValue;
    }

    /**
     * Set a secret in KV and validates that it is not null or empty.
     *
     * @param secretClient secret client
     * @param secretName   name of secret
     * @param secretValue  value of secret
     * @return true if secrete is successfully created in KV.
     */
    public static boolean createKeyVaultSecret(SecretClient secretClient, String secretName, String secretValue) {
        Validators.checkNotNull(secretClient, "secretClient can't be null");
        Validators.checkNotNullAndNotEmpty(secretName, "secretName can't null or empty");
        Validators.checkNotNullAndNotEmpty(secretValue, "secretValue can't be null or empty");

        KeyVaultSecret response;
        try {
            response = secretClient.setSecret(secretName, secretValue);
        } catch (HttpResponseException e) {
            throw new AppException(e.getResponse().getStatusCode(), e.getLocalizedMessage(), e.getMessage());
        }

        return response != null;
    }

    /**
     * Deletes a secret in KV.
     *
     * @param secretClient secret client
     * @param secretName   name of secret
     * @return true if secrete is successfully deleted in KV.
     */
    public static boolean deleteKeyVaultSecret(SecretClient secretClient, String secretName) {
        Validators.checkNotNull(secretClient, "secretClient can't be null");
        Validators.checkNotNullAndNotEmpty(secretName, "secretName can't null or empty");

        SyncPoller<DeletedSecret, Void> deletedSecretVoidSyncPoller = secretClient.beginDeleteSecret(secretName);
        deletedSecretVoidSyncPoller.waitForCompletion();

        return true;
    }
}