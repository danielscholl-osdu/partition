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

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.*;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CloudTableStore {

    @Autowired
    private CloudTable cloudTableClient;

    public boolean deleteCloudTableEntity(final Class<? extends TableEntity> clazzType, String partitionKey, String rowKey) {

        try {
            TableOperation retrievePartition = TableOperation.retrieve(partitionKey, rowKey, clazzType);
            TableEntity partitionEntity = this.cloudTableClient.execute(retrievePartition).getResultAsType();
            TableOperation deleteOperation = TableOperation.delete(partitionEntity);
            this.cloudTableClient.execute(deleteOperation);
            return true;
        } catch (StorageException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error querying cloud table", e.getMessage(), e);
        }
    }

    public Iterable<? extends TableEntity> queryByKey(final Class<? extends TableEntity> clazzType, final String key, final String value) {

        String partitionFilter = TableQuery.generateFilterCondition(
                key,
                TableQuery.QueryComparisons.EQUAL,
                value);

        TableQuery<? extends TableEntity> partitionQuery = TableQuery.from(clazzType)
                .where(partitionFilter);

        try {
            return this.cloudTableClient.execute(partitionQuery);
        } catch (Exception e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "error getting partition", e.getMessage(), e);
        }
    }

    public Iterable<? extends TableEntity> queryByCompoundKey(final Class<? extends TableEntity> clazzType,
                                                              final String rowKey, final String rowValue,
                                                              final String valueKey, final String value) {
        String rowFilter = TableQuery.generateFilterCondition(
                rowKey,
                TableQuery.QueryComparisons.EQUAL,
                rowValue);

        String valueFilter = TableQuery.generateFilterCondition(
                valueKey,
                TableQuery.QueryComparisons.EQUAL,
                value);

        String combinedFilter = TableQuery.combineFilters(rowFilter,
                TableQuery.Operators.AND, valueFilter);

        TableQuery<? extends TableEntity> partitionQuery = TableQuery.from(clazzType)
                .where(combinedFilter);

        try {
            return this.cloudTableClient.execute(partitionQuery);
        } catch (Exception e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "error getting partition", e.getMessage(), e);
        }
    }

    public void insertBatchEntities(TableBatchOperation batchOperation) {
        try {
            this.cloudTableClient.execute(batchOperation);
        } catch (StorageException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "error creating partition", e.getMessage(), e);
        }
    }
}
