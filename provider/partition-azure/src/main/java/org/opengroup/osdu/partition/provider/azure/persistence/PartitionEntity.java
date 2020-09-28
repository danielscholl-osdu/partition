package org.opengroup.osdu.partition.provider.azure.persistence;

import com.microsoft.azure.storage.table.DynamicTableEntity;

public class PartitionEntity extends DynamicTableEntity {

    private String partitionId;

    private String name;

    public PartitionEntity() {}

    public PartitionEntity(String partitionId, String name) {
        super(partitionId, name);

        this.partitionId = partitionId;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) { this.name = name; }

    public String getPartitionId() {
        return this.partitionId;
    }

    public void setPartitionId(String partitionId) { this.partitionId = partitionId; }
}