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

import com.azure.data.tables.models.TableEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PartitionTableStoreTest {

    @InjectMocks
    private PartitionTableStore sut;

    @Mock
    private DataTableStore dataTableStore;

    private static final String PARTITION_ID = "partitionId";
    private static final String PARTITION_KEY = "PartitionKey";

    @Test
    public void should_returnFalse_whenPartitionNotExists() {
        boolean exist = sut.partitionExists(PARTITION_ID);
        assertFalse(exist);
    }

    @Test
    public void should_get_partitionInfo() {
        Collection<TableEntity> list = new ArrayList<>();
        TableEntity tableEntity = new TableEntity(PARTITION_ID, "name");
        list.add(tableEntity);
        when(dataTableStore.queryByKey(PARTITION_KEY, PARTITION_ID)).thenReturn((Iterable) list);

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
    public void should_addPartition_whenPartitionProvided() {
        sut.addPartition(PARTITION_ID, new PartitionInfo());
    }

    @Test
    public void should_returnException_whenNoPartitionInfo() {
        doThrow(new AppException(500, "Error", "error creating partition")).when(dataTableStore).insertBatchEntities(any());
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
        Collection<TableEntity> list = new ArrayList<>();
        TableEntity tableEntity = new TableEntity(PARTITION_ID, "name");
        list.add(tableEntity);
        when(dataTableStore.queryByKey("RowKey", "id")).thenReturn((Iterable) list);

        List<String> partitions = sut.getAllPartitions();
        assertNotNull(partitions);
        assertEquals(1, partitions.size());
    }

    @Test
    public void delete_partition() {
        Collection<TableEntity> list = new ArrayList<>();
        TableEntity tableEntity = new TableEntity(PARTITION_ID, "name");
        list.add(tableEntity);
        when(dataTableStore.queryByKey(PARTITION_KEY, PARTITION_ID)).thenReturn((Iterable) list);
        sut.deletePartition(PARTITION_ID);
    }
}
