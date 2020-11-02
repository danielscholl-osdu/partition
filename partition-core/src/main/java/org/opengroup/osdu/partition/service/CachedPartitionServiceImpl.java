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

package org.opengroup.osdu.partition.service;

import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionServiceCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class CachedPartitionServiceImpl implements IPartitionService {

    private static final String PARTITION_LIST_KEY = "getAllPartitions";

    @Inject
    @Qualifier("partitionServiceImpl")
    private IPartitionService partitionService;

    @Inject
    private IPartitionServiceCache partitionServiceCache;

    @Inject
    private IPartitionServiceCache partitionListCache;

    @Override
    public PartitionInfo createPartition(String partitionId, PartitionInfo partitionInfo) {
        PartitionInfo pi = partitionService.createPartition(partitionId, partitionInfo);

        if (pi != null) {
            partitionServiceCache.put(partitionId, partitionInfo);
        }

        return pi;
    }

    @Override
    public PartitionInfo getPartition(String partitionId) {
        PartitionInfo pi = (PartitionInfo) partitionServiceCache.get(partitionId);

        if (pi == null) {
            pi = partitionService.getPartition(partitionId);

            if (pi != null) {
                partitionServiceCache.put(partitionId, pi);
            }
        }

        return pi;
    }

    @Override
    public boolean deletePartition(String partitionId) {
        if (partitionService.deletePartition(partitionId)) {
            if (partitionServiceCache.get(partitionId) != null) {
                partitionServiceCache.delete(partitionId);
            }

            return true;
        }

        return false;
    }

    @Override
    public List<String> getAllPartitions() {
        List<String> partitions = (List<String>)partitionListCache.get(PARTITION_LIST_KEY);

        if (partitions == null) {
            partitions = partitionService.getAllPartitions();

            if (partitions != null) {
                partitionListCache.put(PARTITION_LIST_KEY, partitions);
            }
        }
        return partitions;
    }
}
