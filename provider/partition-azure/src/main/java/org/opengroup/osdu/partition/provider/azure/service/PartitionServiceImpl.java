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

package org.opengroup.osdu.partition.provider.azure.service;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.azure.persistence.PartitionTableStore;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PartitionServiceImpl implements IPartitionService {

    @Autowired
    private PartitionTableStore tableStore;

    @Override
    public PartitionInfo createPartition(String partitionId, PartitionInfo partitionInfo) {
        if (this.tableStore.partitionExists(partitionId)) {
            throw new AppException(HttpStatus.SC_CONFLICT, "partition exist", "Partition with same id exist");
        }

        this.tableStore.addPartition(partitionId, partitionInfo);

        return partitionInfo;
    }

    @Override
    public PartitionInfo getPartition(String partitionId) {
        Map<String, Property> out = new HashMap<>();
        out.putAll(this.tableStore.getPartition(partitionId));

        if (out.isEmpty()) {
            throw new AppException(HttpStatus.SC_NOT_FOUND, "partition not found", String.format("%s partition not found", partitionId));
        }

        return PartitionInfo.builder().properties(out).build();
    }

    @Override
    public boolean deletePartition(String partitionId) {
        if (!this.tableStore.partitionExists(partitionId)) {
            throw new AppException(HttpStatus.SC_NOT_FOUND, "partition not found", String.format("%s partition not found", partitionId));
        }

        this.tableStore.deletePartition(partitionId);

        return true;
    }
}