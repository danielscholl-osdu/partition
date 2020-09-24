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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.aws.util.SSMHelper;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
public class PartitionServiceImplTest {

    @Mock
    private SSMHelper ssmHelper;
   
    @InjectMocks
    private PartitionServiceImpl partService;

    private PartitionInfo partitionInfo = new PartitionInfo();

    private Map<String,Object> partitionSecretMap = new HashMap<>();

    @Before
    public void setup() {
       
        partitionSecretMap.put("id", "my-tenant");
        partitionSecretMap.put("storageAccount", "storage-account");
        partitionSecretMap.put("complianceRuleSet", "compliance-rule-set");
        partitionInfo.setProperties(partitionSecretMap);

    }

    @Test
    public void should_ThrowConflictError_when_createPartition_whenPartitionExists() {
        when(ssmHelper.partitionExists(any())).thenReturn(true);

        try {
            partService.createPartition(this.partitionInfo.getProperties().get("id").toString(), this.partitionInfo);
            //we should never hit this code because create partition should end in an error
            assertTrue("Expected partService.createPartition to throw an exception, but passed",false);
        } catch (AppException e) {
            assertTrue(e.getError().getCode() == 409);
            assertTrue(e.getError().getReason().equalsIgnoreCase("partition exist"));
            assertTrue(e.getError().getMessage().equalsIgnoreCase("Partition with same id exist"));
        }
    }

    @Test
    public void should_returnPartitionInfo_when_createPartition_whenPartitionDoesntExist() {

        when(ssmHelper.partitionExists(any())).thenReturn(false);
        when(ssmHelper.createOrUpdateSecret(any(), any(), any())).thenReturn(true);
        when(ssmHelper.getSsmParamsPathsForPartition(any())).thenReturn(new ArrayList<String>(this.partitionInfo.getProperties().keySet()));

        PartitionInfo partInfo = partService.createPartition(this.partitionInfo.getProperties().get("id").toString(), this.partitionInfo);
        assertTrue(partInfo.getProperties().size() == 3);
        assertTrue(partInfo.getProperties().containsKey("id"));
        assertTrue(partInfo.getProperties().containsKey("complianceRuleSet"));
        assertTrue(partInfo.getProperties().containsKey("storageAccount"));
    }

    @Test
    public void should_returnPartition_when_partitionExists() {

        String Key1 = "my-tenant-id";
        String Key2 = "my-tenant-groups";
        String Key3 = "my-tenant-complianceRuleSet";

        HashMap<String,Object> propertiesMap = new HashMap<>();
        propertiesMap.put("id", this.partitionInfo.getProperties().get("id").toString());
        propertiesMap.put(Key1, null);
        propertiesMap.put(Key2, null);
        propertiesMap.put(Key3, null);


        when(ssmHelper.getPartitionSecrets(any())).thenReturn(propertiesMap);

        PartitionInfo partitionInfo = this.partService.getPartition(this.partitionInfo.getProperties().get("id").toString());
        assertTrue(partitionInfo.getProperties().containsKey(Key1));
        assertTrue(partitionInfo.getProperties().containsKey(Key2));
        assertTrue(partitionInfo.getProperties().containsKey(Key3));
        assertTrue(partitionInfo.getProperties().containsKey("id"));
    }

    @Test
    public void should_throwNotFoundException_when_partitionDoesntExist() {

        try {
            partService.getPartition(this.partitionInfo.getProperties().get("id").toString());
            //we should never hit this code because get partition should end in an error
            assertTrue("Expected partService.getPartition to throw an exception, but passed",false);
        } catch (AppException e) {
            assertTrue(e.getError().getCode() == 404);
            assertTrue(e.getError().getReason().equalsIgnoreCase("partition not found"));
            assertTrue(e.getError().getMessage().equalsIgnoreCase("my-tenant partition not found"));
        }
    }

    @Test
    public void should_returnTrue_when_successfullyDeletingSecretes() {

        when(ssmHelper.partitionExists(any())).thenReturn(true);
        when(ssmHelper.getSsmParamsPathsForPartition(any())).thenReturn(Arrays.asList("/my-tenant/partition/partitions/dummy-param"));
        when(ssmHelper.deletePartitionSecrets(any())).thenReturn(true);

        assertTrue(this.partService.deletePartition("test-partition"));
    }

    @Test
    public void should_throwException_when_deletingNonExistentPartition() {
        

        try {
            this.partService.deletePartition("some-invalid-partition");
            //we should never hit this code because delete partition should end in an error
            assertTrue("Expected partService.deletePartition to throw an exception, but passed",false);
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