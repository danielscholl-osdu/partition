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

package org.opengroup.osdu.partition.provider.azure.persistence;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class PartitionTableStoreTest {

    @InjectMocks
    private PartitionTableStore sut;

    @Mock
    private CloudTableStore cloudTableStore;

    private static final String PARTITION_ID = "partitionId";
    private static final String PARTITION_KEY = "PartitionKey";

    @Test
    public void should_returnFalse_whenPartitionNotExists() {
        boolean exist = sut.partitionExists(PARTITION_ID);
        assertFalse(exist);
    }

    @Test
    public void should_get_partitionInfo() {
        Collection<PartitionEntity> list = new ArrayList<>();
        PartitionEntity partitionEntity = new PartitionEntity(PARTITION_ID, "name");
        list.add(partitionEntity);
        when(cloudTableStore.queryByKey(PartitionEntity.class, PARTITION_KEY, PARTITION_ID)).thenReturn((Iterable) list);

        Map<String, Property> partition = sut.getPartition(PARTITION_ID);
        assertNotNull(partition);
        assertEquals(1, partition.size());
    }

    @Test
    public void should_returnEmpty_when_partitionNotFound() {
        Map<String, Property> partition = sut.getPartition(PARTITION_ID);
        assertNotNull(partition);
        assertEquals(0, partition.size());
    }

    @Test
    public void should_addPartiton_whenPartionProvided() {
        sut.addPartition(PARTITION_ID, new PartitionInfo());
    }

    @Test
    public void should_returnException_whenNoPartitionInfo() {
        doThrow(new AppException(500, "Error", "error creating partition")).when(cloudTableStore).insertBatchEntities(any());
        try {
            sut.addPartition(PARTITION_ID, new PartitionInfo());
            fail("Should not be here");
        } catch (AppException e) {
            assertEquals(500, e.getError().getCode());
            assertEquals("error creating partition", e.getError().getMessage());
        }
    }


    @Test
    public void should_getAll_partitions() {
        Collection<PartitionEntity> list = new ArrayList<>();
        PartitionEntity partitionEntity = new PartitionEntity(PARTITION_ID, "name");
        list.add(partitionEntity);
        when(cloudTableStore.queryByKey(PartitionEntity.class, "RowKey", "id")).thenReturn((Iterable) list);

        List<String> partitions = sut.getAllPartitions();
        assertNotNull(partitions);
        assertEquals(1, partitions.size());
    }

    @Test
    public void delete_partition() {
        Collection<PartitionEntity> list = new ArrayList<>();
        PartitionEntity partitionEntity = new PartitionEntity(PARTITION_ID, "name");
        list.add(partitionEntity);
        when(cloudTableStore.queryByKey(PartitionEntity.class, PARTITION_KEY, PARTITION_ID)).thenReturn((Iterable) list);
        sut.deletePartition(PARTITION_ID);
    }
}
