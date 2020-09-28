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

    @Autowired
    private CloudTableStore cloudTableStore;

    public void addPartition(String partitionId, PartitionInfo partitionInfo) {

        Map<String, Property> requestProperties = partitionInfo.getProperties();
        TableBatchOperation batchOperation = new TableBatchOperation();
        for (Map.Entry<String, Property> entry : requestProperties.entrySet()) {
            String key = entry.getKey();
            Property property = entry.getValue();

            PartitionEntity partitionEntity = new PartitionEntity(partitionId, key);
            HashMap<String, EntityProperty> properties = new HashMap<>();

            if (property.isSensitive()) {
                property.setValue(this.getTenantSafeSecreteId(partitionId, String.valueOf(property.getValue())));
            }
            properties.put("value", new EntityProperty(String.valueOf(property.getValue())));
            properties.put("sensitive", new EntityProperty(property.isSensitive()));
            partitionEntity.setProperties(properties);
            batchOperation.insertOrReplace(partitionEntity);
        }

        this.cloudTableStore.insertBatchEntities(batchOperation);
    }

    public boolean partitionExists(String partitionId) {

        List<PartitionEntity> partitionEntities = this.queryByPartitionId(partitionId);
        return !partitionEntities.isEmpty();
    }

    public Map<String, Property> getPartition(String partitionId) {
        Map<String, Property> out = new HashMap<>();

        List<PartitionEntity> partitionEntities = this.queryByPartitionId(partitionId);
        if (partitionEntities.isEmpty()) {
            return out;
        }

        for (PartitionEntity pe : partitionEntities) {
            Property property = Property.builder().build();
            HashMap<String, EntityProperty> properties = pe.getProperties();
            if (properties.containsKey("sensitive")) {
                property.setSensitive(properties.get("sensitive").getValueAsBoolean());
            }
            if (properties.containsKey("value")) {
                property.setValue(properties.get("value").getValueAsString());
            }
            out.put(pe.getRowKey(), property);
        }
        return out;
    }

    public List<PartitionEntity> queryByPartitionId(String partitionId) {
        List<PartitionEntity> out = new ArrayList<>();
        Iterable<PartitionEntity> results = (Iterable<PartitionEntity>) this.cloudTableStore.queryByPartitionId(PartitionEntity.class, partitionId);
        for (PartitionEntity tableEntity : results) {
            tableEntity.setPartitionId(tableEntity.getPartitionKey());
            tableEntity.setName(tableEntity.getRowKey());
            out.add(tableEntity);
        }
        return out;
    }

    public void deletePartition(String partitionId) {
        Iterable<PartitionEntity> results = (Iterable<PartitionEntity>) this.cloudTableStore.queryByPartitionId(PartitionEntity.class, partitionId);
        for (PartitionEntity tableEntity : results) {
            this.cloudTableStore.deleteCloudTableEntity(PartitionEntity.class, tableEntity.getPartitionKey(), tableEntity.getRowKey());
        }
    }

    private String getTenantSafeSecreteId(String partitionId, String secreteName) {
        return String.format("%s-%s", partitionId, secreteName);
    }
}
