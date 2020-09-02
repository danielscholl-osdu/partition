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
import com.azure.core.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.AppException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeyVaultFacadeTest {

    @Mock
    private SecretClient kv;

    @Mock
    private KeyVaultSecret keyVaultSecret;

    @Mock
    private PagedIterable<SecretProperties> secretProperties;

    @Mock
    private Iterable<PagedResponse<SecretProperties>> propertiesIterable;

    @Mock
    private Iterator<PagedResponse<SecretProperties>> iterator;

    @Mock
    private PagedResponse<SecretProperties> resp;

    @Mock
    private KeyVaultSecret secret;

    @Mock
    private SyncPoller<DeletedSecret, Void> deletedSecretVoidSyncPoller;

    @Test
    public void should_returnTrue_ifSecretExists() {
        when(kv.getSecret(any())).thenReturn(keyVaultSecret);
        assertTrue(KeyVaultFacade.secretExists(kv, "secret"));
    }

    @Test
    public void should_returnFalse_ifSecretNotExists() {
        when(kv.getSecret(any())).thenThrow(mock(ResourceNotFoundException.class));
        assertFalse(KeyVaultFacade.secretExists(kv, "secret"));
    }

    @Test
    public void should_returnCorrectSecrets() {
        List<SecretProperties> secretPropertyItems = new ArrayList<>();
        SecretProperties secretProperty = mock(SecretProperties.class);
        secretPropertyItems.add(secretProperty);
        final String key = "cosmos-endpoint-secret";

        when(kv.listPropertiesOfSecrets()).thenReturn(secretProperties);
        when(secretProperties.iterableByPage()).thenReturn(propertiesIterable);
        when(propertiesIterable.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(resp);
        when(resp.getItems()).thenReturn(secretPropertyItems);
        when(secretProperty.getName()).thenReturn(key);

        assertEquals(Collections.singletonList(key), KeyVaultFacade.getKeyVaultSecrets(kv, "cosmos-endpoint-secret"));
    }

    @Test
    public void should_throwNotFoundException_ifPartitionNotFound() {
        when(kv.getSecret(any())).thenThrow(ResourceNotFoundException.class);

        try {
            KeyVaultFacade.getKeyVaultSecret(kv, "secret");
            fail("Method didn't throw when I expected it to");
        } catch (AppException e) {
            assertEquals(HttpStatus.SC_NOT_FOUND, e.getError().getCode());
            assertEquals("secret partition not found", e.getError().getMessage());
        } catch (Exception e) {
            fail("Wrong exception is thrown");
        }
    }

    @Test
    public void should_throwIllegalStateException_ifSecretIsNull() {
        when(kv.getSecret(any())).thenReturn(null);

        try {
            KeyVaultFacade.getKeyVaultSecret(kv, "secret");
            fail("Method didn't throw when I expected it to");
        } catch (IllegalStateException e) {
            assertEquals("No secret found with name secret", e.getMessage());
        } catch (Exception e) {
            fail("Wrong exception is thrown");
        }

        when(kv.getSecret(any())).thenReturn(secret);

        try {
            KeyVaultFacade.getKeyVaultSecret(kv, "secret");
            fail("Method didn't throw when I expected it to");
        } catch (IllegalStateException e) {
            assertEquals("Secret unexpectedly missing from KeyVault response for secret with name secret", e.getMessage());
        } catch (Exception e) {
            fail("Wrong exception is thrown");
        }
    }

    @Test
    public void should_returnTrue_CreateKeyVaultSecretSuccessfully() {
        when(kv.setSecret(any(), any())).thenReturn(secret);
        assertTrue(KeyVaultFacade.createKeyVaultSecret(kv, "secret", "value"));
    }

    @Test
    public void should_throwAppResponseException_ifHttpResponseExceptionThrown() {
        HttpResponseException httpResponseException = mock(HttpResponseException.class);
        HttpResponse response = mock(HttpResponse.class);
        when(kv.setSecret(any(), any())).thenThrow(httpResponseException);
        when(httpResponseException.getResponse()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(500);
        when(httpResponseException.getLocalizedMessage()).thenReturn("error");
        when(httpResponseException.getMessage()).thenReturn("error");

        try {
            KeyVaultFacade.createKeyVaultSecret(kv, "secret", "value");
            fail("Method didn't throw when I expected it to");
        } catch (AppException e) {
            assertEquals(500, e.getError().getCode());
        } catch (Exception e) {
            fail("Wrong exception is thrown");
        }
    }
    @Test
    public void should_returnTrue_DeleteKeyVaultSecretSuccessfully() {
        when(kv.beginDeleteSecret(any())).thenReturn(deletedSecretVoidSyncPoller);
        when(deletedSecretVoidSyncPoller.waitForCompletion()).thenReturn(null);
        assertTrue(KeyVaultFacade.deleteKeyVaultSecret(kv, "secret"));
    }
}