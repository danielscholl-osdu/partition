// Copyright © 2020 Amazon Web Services
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

package org.opengroup.osdu.partition.provider.aws.service;

import org.apache.http.HttpStatus;
import org.bson.types.Binary;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.DefaultLogger;
import org.opengroup.osdu.core.common.logging.ILogger;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.aws.model.Partition;
import org.opengroup.osdu.partition.provider.aws.model.IPartitionRepository;
import org.opengroup.osdu.partition.provider.aws.util.AwsKmsEncryptionClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class PartitionServiceImplTest {

    @Mock
    private IPartitionRepository repository;

    @Mock
    private AwsKmsEncryptionClient awsKmsEncryptionClient;

    @Mock
    private JaxRsDpsLog logger = new JaxRsDpsLog(new DefaultLogger(), new DpsHeaders());

    @Captor
    private ArgumentCaptor<Partition> partitionArgumentCaptor;

    @InjectMocks
    private PartitionServiceImpl partService;

    private String id;

    private final PartitionInfo partitionInfoDummy = new PartitionInfo();

    private final PartitionInfo partitionInfoWithId = new PartitionInfo();

    private final Partition partitionDummy = new Partition();
    private final Partition encryptedPartitionDummy = new Partition();

    private final List<Partition> allPartitions = new ArrayList<>();

    @Before
    public void setup() {

        ReflectionTestUtils.setField(partService, "logger", logger);

        id = "id";

        Map<String, Property> partitionSecretMap = new HashMap<>();
        partitionSecretMap.put("storageAccount", Property.builder()
                .value("storage-account")
                .sensitive(true).build());
        partitionSecretMap.put("complianceRuleSet", Property.builder()
                .value("compliance-rule-set")
                .sensitive(false).build());

        partitionInfoDummy.setProperties(partitionSecretMap);

        partitionDummy.setId(id);
        partitionDummy.setProperties(partitionSecretMap);

        Map<String, Property> encryptedPartitionSecretMap = new HashMap<>();
        encryptedPartitionSecretMap.put("storageAccount", Property.builder()
                .value(new Binary("storage-account".getBytes(StandardCharsets.UTF_8)))
                .sensitive(true).build());
        encryptedPartitionSecretMap.put("complianceRuleSet", Property.builder()
                .value("compliance-rule-set")
                .sensitive(false).build());

        encryptedPartitionDummy.setId(id);
        encryptedPartitionDummy.setProperties(encryptedPartitionSecretMap);

        Map<String, Property> partitionsInfoWithIdMap = new HashMap<>(partitionSecretMap);
        partitionsInfoWithIdMap.put("id", Property.builder().value(id).sensitive(false).build());
        partitionInfoWithId.setProperties(partitionsInfoWithIdMap);

        allPartitions.add(partitionDummy);
        allPartitions.add(encryptedPartitionDummy);
   }

    @Test
    public void should_ThrowConflictError_when_createPartition_whenPartitionExists() {
        when(repository.findById(any())).thenReturn(Optional.of(partitionDummy));

        try {
            partService.createPartition(id, partitionInfoDummy);
            //we should never hit this code because create partition should end in an error
            fail("Expected partService.createPartition to throw an exception, but passed");
        } catch (AppException e) {
            assertEquals(409, e.getError().getCode());
            assertTrue(e.getError().getReason().equalsIgnoreCase("partition exist"));
            assertTrue(e.getError().getMessage().equalsIgnoreCase("Partition with same id exist"));
        }
    }

    @Test
    public void should_ThrowAppException_when_updatePartition_whenPartitionDoesntExist() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        try {
            partService.updatePartition(id, partitionInfoDummy);
            //we should never hit this code because create partition should end in an error
            fail("Expected partService.createPartition to throw an exception, but passed");
        } catch (AppException e) {
            assertEquals(404, e.getError().getCode());
            assertTrue(e.getError().getReason().equalsIgnoreCase("Partition does not exist"));
            assertTrue(e.getError().getMessage().equalsIgnoreCase("Partition does not exist"));
        }
    }

    @Test
    public void should_ThrowAppException_when_updatePartition_whenPartitionIdChanges() {
        when(repository.findById(any())).thenReturn(Optional.of(partitionDummy));

        try {
            partService.updatePartition(id, partitionInfoWithId);
            //we should never hit this code because create partition should end in an error
            fail("Expected partService.createPartition to throw an exception, but passed");
        } catch (AppException e) {
            assertEquals(400, e.getError().getCode());
            assertTrue(e.getError().getReason().equalsIgnoreCase("Cannot update id"));
            assertTrue(e.getError().getMessage().equalsIgnoreCase("the field id cannot be updated"));
        }
    }

    @Test
    public void should_savePartition_when_updatePartition_whenPartitionExists() {
        when(repository.findById(any())).thenReturn(Optional.of(partitionDummy));
        when(repository.save(any())).thenReturn(partitionDummy);

        assertDoesNotThrow(() -> partService.updatePartition(id, partitionInfoDummy));
    }

    @Test
    public void should_ThrowAppException_when_updatePartition_whenRepositorySaveErrors() {
        when(repository.findById(any())).thenReturn(Optional.of(partitionDummy));
        doThrow(RuntimeException.class).when(repository).save(any());

        try {
            partService.updatePartition(id, partitionInfoDummy);
            //we should never hit this code because create partition should end in an error
            fail("Expected partService.createPartition to throw an exception, but passed");
        } catch (AppException e) {
            assertEquals(500, e.getError().getCode());
            assertTrue(e.getError().getReason().equalsIgnoreCase("Partition update Failure"));
        }
    }

    @Test
    public void should_returnPartitionInfo_when_createPartition_whenPartitionDoesntExist() {

        when(repository.findById(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(partitionDummy);
        when(awsKmsEncryptionClient.encrypt(any(), any())).thenReturn("ENCRYPTED".getBytes(StandardCharsets.UTF_8));

        PartitionInfo partInfo = partService.createPartition(partitionDummy.getId(), partitionInfoDummy);
        assertEquals(2, partInfo.getProperties().size());

        for (Map.Entry<String, Property> e : partitionInfoDummy.getProperties().entrySet()) {
            assertTrue(partInfo.getProperties().containsKey(e.getKey()));
        }
    }

    @Test
    public void should_returnPartition_when_partitionExists() {

        when(repository.findById(any())).thenReturn(Optional.of(encryptedPartitionDummy));
        when(awsKmsEncryptionClient.decrypt(any(), any())).thenReturn("DECRYPTED");

        PartitionInfo partitionInfo = partService.getPartition(id);

        assertTrue(partitionInfo.getProperties().containsKey("complianceRuleSet"));
        assertTrue(partitionInfo.getProperties().containsKey("storageAccount"));
    }

    @Test
    public void should_ThrowAppException_when_decryptionClassCastExceptionThrown() {

        when(repository.findById(any())).thenReturn(Optional.of(encryptedPartitionDummy));
        doThrow(ClassCastException.class).when(awsKmsEncryptionClient).decrypt(any(), any());

        try {
            PartitionInfo partitionInfo = partService.getPartition(id);
            fail("Expected partService.getPartition to throw an exception, but passed");
        } catch (AppException exception) {
            assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, exception.getError().getCode());
            assertTrue(exception.getError().getReason().equalsIgnoreCase("Corrupt data"));
        }
    }

    @Test
    public void should_ThrowAppException_when_decryptionIllegalStateExceptionThrown() {

        when(repository.findById(any())).thenReturn(Optional.of(encryptedPartitionDummy));
        doThrow(IllegalStateException.class).when(awsKmsEncryptionClient).decrypt(any(), any());

        try {
            PartitionInfo partitionInfo = partService.getPartition(id);
            fail("Expected partService.getPartition to throw an exception, but passed");
        } catch (AppException exception) {
            assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, exception.getError().getCode());
            assertTrue(exception.getError().getReason().equalsIgnoreCase("Illegal database modification"));
        }
    }

    @Test
    public void should_returnList_when_getAllPartitionsReturns() {

        when(repository.findAll()).thenReturn(allPartitions);

        List<String> allPartitionIds = partService.getAllPartitions();
        assertEquals(2, allPartitionIds.size());

        for (String resultId : allPartitionIds) {
            assertEquals(id, resultId);
        }
    }

    @Test
    public void should_call_awsEncryptionClient_encrypt_when_isSensitive() {

        when(repository.findById(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(partitionDummy);
        when(awsKmsEncryptionClient.encrypt(any(), any())).thenReturn("ENCRYPTED".getBytes(StandardCharsets.UTF_8));

        partService.createPartition(partitionDummy.getId(), partitionInfoDummy);

        verify(repository).save(partitionArgumentCaptor.capture());
        Map<String, Property> encryptedProps = partitionArgumentCaptor.getValue().getProperties();

        for (Map.Entry<String, Property> e : partitionInfoDummy.getProperties().entrySet()) {
            if (e.getValue().isSensitive()) {
                // just check to see if the sensitive value has been modified
                assertNotEquals(e.getValue().getValue(), encryptedProps.get(e.getKey()).getValue());
            } else {
                assertEquals(e.getValue().getValue(), encryptedProps.get(e.getKey()).getValue());
            }
        }
    }

    @Test
    public void should_call_awsEncryptionClient_decrypt_when_isSensitive() {
        final String decrypted = "DECRYPTED";

        when(repository.findById(any())).thenReturn(Optional.of(encryptedPartitionDummy));
        when(awsKmsEncryptionClient.decrypt(any(), any())).thenReturn(decrypted);

        PartitionInfo partitionInfo = partService.getPartition(id);
        Map<String, Property> encryptedProps = partitionInfo.getProperties();

        for (Map.Entry<String, Property> e : partitionInfoDummy.getProperties().entrySet()) {
            if (e.getValue().isSensitive()) {
                assertEquals(decrypted, encryptedProps.get(e.getKey()).getValue());
            } else {
                assertEquals(e.getValue().getValue(), encryptedProps.get(e.getKey()).getValue());
            }
        }

        assertTrue(partitionInfo.getProperties().containsKey("storageAccount"));
    }

    @Test
    public void should_throwNotFoundException_when_partitionDoesntExist() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        try {
            partService.getPartition("my-tenant");
            //we should never hit this code because get partition should end in an error
            fail("Expected partService.getPartition to throw an exception, but passed");
        } catch (AppException e) {
            assertEquals(404, e.getError().getCode());
            assertTrue(e.getError().getReason().equalsIgnoreCase("partition not found"));
            assertTrue(e.getError().getMessage().equalsIgnoreCase("my-tenant partition not found"));
        }
    }

    @Test
    public void should_returnTrue_when_successfullyDeletingSecretes() {

        when(repository.findById(any())).thenReturn(Optional.of(partitionDummy));

        assertTrue(partService.deletePartition("test-partition"));
    }

    @Test
    public void should_throwException_when_deletingNonExistentPartition() {

        try {
            this.partService.deletePartition("some-invalid-partition");
            //we should never hit this code because delete partition should end in an error
            fail("Expected partService.deletePartition to throw an exception, but passed");
        } catch (AppException ae) {
            assertEquals(404, ae.getError().getCode());
            assertEquals("some-invalid-partition partition not found", ae.getError().getMessage());
        }
    }

    @Test(expected = AppException.class)
    public void should_throwException_when_deletingInvalidPartition() {

        this.partService.deletePartition(null);
    }

}
