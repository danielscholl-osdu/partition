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

import org.bson.types.Binary;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.aws.model.Partition;
import org.opengroup.osdu.partition.provider.aws.model.IPartitionRepository;
import org.opengroup.osdu.partition.provider.aws.util.AwsKmsEncryptionClient;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;


@RunWith(MockitoJUnitRunner.class)
public class PartitionServiceImplTest {

    @Mock
    private IPartitionRepository repository;

    @Mock
    private AwsKmsEncryptionClient awsKmsEncryptionClient;

    @Captor
    private ArgumentCaptor<Partition> partitionArgumentCaptor;

    @InjectMocks
    private PartitionServiceImpl partService;

    private String id;
    private PartitionInfo partitionInfoDummy = new PartitionInfo();
    private Partition partitionDummy = new Partition();
    private Partition encryptedPartitionDummy = new Partition();

    @Before
    public void setup() {

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

    }

    @Test
    public void should_ThrowConflictError_when_createPartition_whenPartitionExists() {
        when(repository.findById(any())).thenReturn(Optional.of(partitionDummy));

        try {
            partService.createPartition(id, partitionInfoDummy);
            //we should never hit this code because create partition should end in an error
            assertTrue("Expected partService.createPartition to throw an exception, but passed", false);
        } catch (AppException e) {
            assertTrue(e.getError().getCode() == 409);
            assertTrue(e.getError().getReason().equalsIgnoreCase("partition exist"));
            assertTrue(e.getError().getMessage().equalsIgnoreCase("Partition with same id exist"));
        }
    }

    @Test
    public void should_returnPartitionInfo_when_createPartition_whenPartitionDoesntExist() {

        when(repository.findById(any())).thenReturn(Optional.empty());
        when(repository.save(any())).thenReturn(partitionDummy);
        when(awsKmsEncryptionClient.encrypt(any(), any())).thenReturn("ENCRYPTED".getBytes(StandardCharsets.UTF_8));

        PartitionInfo partInfo = partService.createPartition(partitionDummy.getId(), partitionInfoDummy);
        assertTrue(partInfo.getProperties().size() == 2);

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
                assertTrue(encryptedProps.get(e.getKey()).getValue() != e.getValue().getValue());
            } else {
                assertTrue(encryptedProps.get(e.getKey()).getValue() == e.getValue().getValue());
            }
        }
    }

    @Test
    public void should_call_awsEncryptionClient_decrypt_when_isSensitive() {

        when(repository.findById(any())).thenReturn(Optional.of(encryptedPartitionDummy));
        when(awsKmsEncryptionClient.decrypt(any(), any())).thenReturn("DECRYPTED");

        PartitionInfo partitionInfo = partService.getPartition(id);
        Map<String, Property> encryptedProps = partitionInfo.getProperties();

        for (Map.Entry<String, Property> e : partitionInfoDummy.getProperties().entrySet()) {
            if (e.getValue().isSensitive()) {
                assertTrue(encryptedProps.get(e.getKey()).getValue().equals("DECRYPTED"));
            } else {
                assertTrue(encryptedProps.get(e.getKey()).getValue() == e.getValue().getValue());
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
            assertTrue("Expected partService.getPartition to throw an exception, but passed", false);
        } catch (AppException e) {
            assertTrue(e.getError().getCode() == 404);
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
            assertTrue("Expected partService.deletePartition to throw an exception, but passed", false);
        } catch (AppException ae) {
            assertTrue(ae.getError().getCode() == 404);
            assertEquals("some-invalid-partition partition not found", ae.getError().getMessage());
        }
    }

    @Test(expected = AppException.class)
    public void should_throwException_when_deletingInvalidPartition() {

        this.partService.deletePartition(null);
    }

}