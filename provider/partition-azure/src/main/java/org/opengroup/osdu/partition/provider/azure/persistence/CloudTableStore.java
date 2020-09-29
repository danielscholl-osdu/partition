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

        return this.cloudTableClient.execute(partitionQuery);
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

        return this.cloudTableClient.execute(partitionQuery);
    }

    public void insertBatchEntities(TableBatchOperation batchOperation) {
        try {
            this.cloudTableClient.execute(batchOperation);
        } catch (StorageException e) {
            new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "error creating partition", e.getMessage(), e);
        }
    }
}
