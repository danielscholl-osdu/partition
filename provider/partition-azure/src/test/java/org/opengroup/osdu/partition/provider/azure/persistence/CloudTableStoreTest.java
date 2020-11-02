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
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, eitsher express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.partition.provider.azure.persistence;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.powermock.modules.junit4.PowerMockRunner;


import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class CloudTableStoreTest {

    @Mock
    private CloudTable cloudTableClient;

    @InjectMocks
    private CloudTableStore sut;

    @Test
    public void should_empty_whenRecordNotExists() {
        Iterable<PartitionEntity> results = (Iterable<PartitionEntity>) sut.queryByKey(PartitionEntity.class,
                "partitionKey", "partitionId");
        assertNotNull(results);
    }

    @Test
    public void should_empty_whenRecordExists() throws StorageException {
        try {
            TableBatchOperation tbOp = new TableBatchOperation();
            when(cloudTableClient.execute(new TableBatchOperation())).thenThrow(new StorageException("Error", "Error", null));
            sut.insertBatchEntities(tbOp);
            fail("should not be here");
        } catch (AppException e) {
            assertEquals(500, e.getError().getCode());
            assertEquals("error creating partition", e.getError().getReason());
        }
    }

    @Test
    public void when_call_queryByCompoundKey() {
        Iterable<? extends TableEntity> result = sut.queryByCompoundKey(PartitionEntity.class, "RowKey", "id", "value", "partitionId");
        assertNotNull(result);
    }

    @Test
    public void when_wrongInput_ThrowException() {
        try {
            sut.queryByCompoundKey(PartitionEntity.class, null, null, null, null);
            fail("Should not be here");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void when_call_queryByKey() {
        Iterable<? extends TableEntity> result = sut.queryByKey(PartitionEntity.class, "PartitionKey", "partitionId");
        assertNotNull(result);
    }

    @Test
    public void when_call_queryByKey_wrongInput_ThrowException() {
        try {
            sut.queryByKey(PartitionEntity.class, null, null);
            fail("Should not be here");
        } catch (Exception e) {
            assertNotNull(e);
        }
    }
}
