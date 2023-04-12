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

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableTransactionAction;
import io.jsonwebtoken.lang.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.validation.ValidationException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.*;

@RunWith(PowerMockRunner.class)
public class DataTableStoreTest {

    @Mock
    private TableClient tableClient;

    @InjectMocks
    private DataTableStore sut;

    @Test
    public void should_empty_whenRecordNotExists() {
        PagedIterable<TableEntity> response = mock(PagedIterable.class);
        when(tableClient.listEntities(any(), any(), any())).thenReturn(response);
        Iterable<TableEntity> results = sut.queryByKey("partitionKey", "partitionId");
        assertNotNull(results);
    }


    @Test
    public void should_not_throw_whenEmptyRecordsUploaded() throws Exception {
        List<TableTransactionAction> actionList = new ArrayList<>();
        //valid input -> no exception should be thrown
        //create a more logical test
         sut.insertBatchEntities(actionList);
    }

    @Test
    public void when_call_queryByCompoundKey() {
        PagedIterable<TableEntity> response = mock(PagedIterable.class);
        when(tableClient.listEntities(any(), any(), any())).thenReturn(response);
        Iterable<TableEntity> result = sut.queryByCompoundKey("RowKey", "id", "value", "partitionId");
        assertNotNull(result);
    }

    @Test
    public void when_wrongInput_queryByCompoundKey_ReturnNull() {
        try {
            Iterable<TableEntity> outPut = sut.queryByCompoundKey( null, null, null, null);
            Assert.isNull(outPut);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }


    @Test
    public void when_call_queryByKey_wrongInput_ShouldReturnNull() {
        try {
            Iterable<TableEntity> outPut = sut.queryByKey(null, null);
            assertNull(outPut);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void when_queryByKey_invalid_input_ExceptionIsThrown(){
        try {
            sut.queryByKey("partitionKey", "invalid '" );
        }
        catch (Exception e){
            assertNotNull(e);
            assertEquals(e.getClass(), ValidationException.class);
            assertEquals(e.getMessage().toString(), "Invalid input parameters, value contains illegal character(s)");

        }
    }
    @Test
    public void when_queryByCompoundKey_invalid_input_ExceptionIsThrown(){
        try {
            sut.queryByCompoundKey("rowKey", "id","partitionKey", "invalid '" );
        }
        catch (Exception e){
            assertNotNull(e);
            assertEquals(e.getClass(), ValidationException.class);
            assertEquals(e.getMessage().toString(), "Invalid input parameters, value contains illegal character(s)");

        }
    }
}
