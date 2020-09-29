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

import com.microsoft.azure.storage.table.EntityProperty;
import com.microsoft.azure.storage.table.TableBatchOperation;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PartitionTableStore {

    private final static String ID = "id";
    private final static String VALUE = "value";
    private final static String SENSITIVE = "sensitive";

    private final static String PARTITION_KEY = "PartitionKey";
    private final static String ROW_KEY = "RowKey";

    @Autowired
    private CloudTableStore cloudTableStore;

    public void addPartition(String partitionId, PartitionInfo partitionInfo) {

        Map<String, Property> requestProperties = partitionInfo.getProperties();
        TableBatchOperation batchOperation = new TableBatchOperation();
        batchOperation.insertOrReplace(this.getIdPartitionEntity(partitionId));
        for (Map.Entry<String, Property> entry : requestProperties.entrySet()) {
            String key = entry.getKey();
            Property property = entry.getValue();

            PartitionEntity partitionEntity = new PartitionEntity(partitionId, key);
            HashMap<String, EntityProperty> properties = new HashMap<>();

            if (property.isSensitive()) {
                property.setValue(this.getTenantSafeSecreteId(partitionId, String.valueOf(property.getValue())));
            }
            properties.put(VALUE, new EntityProperty(String.valueOf(property.getValue())));
            properties.put(SENSITIVE, new EntityProperty(property.isSensitive()));
            partitionEntity.setProperties(properties);
            batchOperation.insertOrReplace(partitionEntity);
        }

        this.cloudTableStore.insertBatchEntities(batchOperation);
    }

    public boolean partitionExists(String partitionId) {
        List<PartitionEntity> partitionEntities = this.queryById(partitionId);
        return partitionEntities.size() == 1;
    }

    public Map<String, Property> getPartition(String partitionId) {
        Map<String, Property> out = new HashMap<>();

        List<PartitionEntity> partitionEntities = this.getAllByPartitionId(partitionId);
        if (partitionEntities.isEmpty()) {
            return out;
        }

        for (PartitionEntity pe : partitionEntities) {
            Property property = Property.builder().build();
            HashMap<String, EntityProperty> properties = pe.getProperties();
            if (properties.containsKey(SENSITIVE)) {
                property.setSensitive(properties.get(SENSITIVE).getValueAsBoolean());
            }
            if (properties.containsKey(VALUE)) {
                property.setValue(properties.get(VALUE).getValueAsString());
            }
            out.put(pe.getRowKey(), property);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public void deletePartition(String partitionId) {
        Iterable<PartitionEntity> results = (Iterable<PartitionEntity>)
                this.cloudTableStore.queryByKey(PartitionEntity.class,
                PARTITION_KEY, partitionId);
        for (PartitionEntity tableEntity : results) {
            this.cloudTableStore.deleteCloudTableEntity(PartitionEntity.class, tableEntity.getPartitionKey(), tableEntity.getRowKey());
        }
    }

    @SuppressWarnings("unchecked")
    private List<PartitionEntity> queryById(String partitionId) {
        List<PartitionEntity> out = new ArrayList<>();
        Iterable<PartitionEntity> results = (Iterable<PartitionEntity>)
                this.cloudTableStore.queryByCompoundKey(PartitionEntity.class,
                ROW_KEY, ID,
                VALUE, partitionId);
        for (PartitionEntity tableEntity : results) {
            out.add(tableEntity);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<PartitionEntity> getAllByPartitionId(String partitionId) {
        List<PartitionEntity> out = new ArrayList<>();
        Iterable<PartitionEntity> results = (Iterable<PartitionEntity>)
                this.cloudTableStore.queryByKey(PartitionEntity.class,
                PARTITION_KEY, partitionId);
        for (PartitionEntity tableEntity : results) {
            tableEntity.setPartitionId(tableEntity.getPartitionKey());
            tableEntity.setName(tableEntity.getRowKey());
            out.add(tableEntity);
        }
        return out;
    }

    private PartitionEntity getIdPartitionEntity(String partitionId) {
        PartitionEntity partitionEntity = new PartitionEntity(partitionId, ID);
        HashMap<String, EntityProperty> properties = new HashMap<>();
        properties.put(VALUE, new EntityProperty(partitionId));
        properties.put(SENSITIVE, new EntityProperty(false));
        partitionEntity.setProperties(properties);
        return partitionEntity;
    }

    private String getTenantSafeSecreteId(String partitionId, String secreteName) {
        return String.format("%s-%s", partitionId, secreteName);
    }
}
