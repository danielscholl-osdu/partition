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