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

package org.opengroup.osdu.partition.provider.azure.service;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.azure.utils.KeyVaultFacade;
import org.opengroup.osdu.partition.provider.azure.utils.ThreadPoolService;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(KeyVaultFacade.class)
public class PartitionServiceImplTest {

    @Mock
    private SecretClient keyVaultClient;
    @Mock
    private ThreadPoolService threadPoolService;
    @Mock
    private TelemetryClient telemetryClient;
    @InjectMocks
    private PartitionServiceImpl sut;

    private PartitionInfo partitionInfo = new PartitionInfo();

    private KeyVaultSecret keyVaultSecret = new KeyVaultSecret("myKey", "myValue");

    @Before
    public void setup() {
        mockStatic(KeyVaultFacade.class);

        Map<String, Object> properties = new HashMap<>();
        properties.put("id", "my-tenant");
        properties.put("storageAccount", "storage-account");
        properties.put("complianceRuleSet", "compliance-rule-set");
        partitionInfo.setProperties(properties);
        doNothing().when(telemetryClient).trackException(any(Exception.class));
    }

    @Test
    public void should_ThrowConflictError_when_createPartition_whenPartitionExists() {
        when(keyVaultClient.getSecret(any())).thenReturn(keyVaultSecret);

        try {
            sut.createPartition(this.partitionInfo.getProperties().get("id").toString(), this.partitionInfo);
        } catch (AppException e) {
            assertTrue(e.getError().getCode() == 409);
            assertTrue(e.getError().getReason().equalsIgnoreCase("partition exist"));
            assertTrue(e.getError().getMessage().equalsIgnoreCase("Partition with same id exist"));
        }
    }

    @Test
    public void should_returnPartitionInfo_when_createPartition_whenPartitionDoesntExist() {
        when(keyVaultClient.getSecret(any())).thenThrow(ResourceNotFoundException.class);
        when(keyVaultClient.setSecret(any(), any())).thenReturn(keyVaultSecret);

        PartitionInfo partInfo = sut.createPartition(this.partitionInfo.getProperties().get("id").toString(), this.partitionInfo);
        assertTrue(partInfo.getProperties().size() == 3);
        assertTrue(partInfo.getProperties().containsKey("id"));
        assertTrue(partInfo.getProperties().containsKey("complianceRuleSet"));
        assertTrue(partInfo.getProperties().containsKey("storageAccount"));
    }

    @Test
    public void should_returnPartition_when_partitionExists() {
        when(keyVaultClient.getSecret(any())).thenReturn(keyVaultSecret);
        when(KeyVaultFacade.secretExists(any(), anyString())).thenReturn(true);
        when(KeyVaultFacade.getKeyVaultSecrets(any(), anyString())).thenReturn(Arrays.asList("my-tenant-id", "my-tenant-complianceRuleSet", "my-tenant-groups"));
        when(KeyVaultFacade.getKeyVaultSecret(this.keyVaultClient, "my-tenant-id")).thenReturn("my-tenant");
        when(KeyVaultFacade.getKeyVaultSecret(this.keyVaultClient, "my-tenant-groups")).thenReturn("[\"service.storage.admin\"]");
        when(KeyVaultFacade.getKeyVaultSecret(this.keyVaultClient, "my-tenant-complianceRuleSet")).thenReturn("shared");

        PartitionInfo partitionInfo = this.sut.getPartition(this.partitionInfo.getProperties().get("id").toString());
        assertTrue(partitionInfo.getProperties().containsValue("my-tenant"));
        assertTrue(partitionInfo.getProperties().containsKey("groups"));
        assertTrue(partitionInfo.getProperties().containsKey("complianceRuleSet"));
        assertTrue(partitionInfo.getProperties().containsKey("id"));
    }

    @Test
    public void should_throwNotFoundException_when_partitionDoesntExist() {
        when(keyVaultClient.getSecret(any())).thenThrow(ResourceNotFoundException.class);

        try {
            sut.getPartition(this.partitionInfo.getProperties().get("id").toString());
        } catch (AppException e) {
            assertTrue(e.getError().getCode() == 404);
            assertTrue(e.getError().getReason().equalsIgnoreCase("partition not found"));
            assertTrue(e.getError().getMessage().equalsIgnoreCase("my-tenant partition not found"));
        }
    }

    @Test
    public void should_returnTrue_when_successfullyDeletingSecretes() {
        when(KeyVaultFacade.secretExists(any(), anyString())).thenReturn(true);
        when(KeyVaultFacade.getKeyVaultSecrets(any(), anyString())).thenReturn(Arrays.asList("dummy-id"));
        when(KeyVaultFacade.deleteKeyVaultSecret(any(), anyString())).thenReturn(true);
        when(this.threadPoolService.getExecutorService()).thenReturn(Executors.newFixedThreadPool(2));

        assertTrue(this.sut.deletePartition("test-partition"));
    }

    @Test
    public void should_throwException_when_deletingNonExistentPartition() {
        when(KeyVaultFacade.getKeyVaultSecret(this.keyVaultClient, "test-partition-id")).thenReturn("");

        try {
            this.sut.deletePartition("test-partition");
        } catch (AppException ae) {
            assertTrue(ae.getError().getCode() == 404);
            assertEquals("test-partition partition not found", ae.getError().getMessage());
        }
    }

    @Test(expected = AppException.class)
    public void should_throwException_when_deletingInvalidPartition() {

        this.sut.deletePartition(null);
    }
}