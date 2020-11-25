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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionServiceCache;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CachedPartitionServiceImplTest {

    @Mock
    private IPartitionService partitionServiceImpl;

    @Mock
    private IPartitionServiceCache<String, PartitionInfo> partitionServiceCache;

    @Mock
    private IPartitionServiceCache<String, List<String>> partitionListCache;

    @InjectMocks
    private CachedPartitionServiceImpl cachedPartitionServiceImpl;

    @Test
    public void createPartitionSucceed() {
        String partId = "key";

        PartitionInfo newPi = PartitionInfo.builder().build();
        PartitionInfo retPi = PartitionInfo.builder().build();

        when(partitionServiceImpl.createPartition(partId, newPi)).thenReturn(retPi);

        cachedPartitionServiceImpl.createPartition(partId, newPi);

        verify(partitionServiceImpl, times(1)).createPartition(partId, newPi);
        verify(partitionServiceCache, times(1)).put(partId, retPi);
    }

    @Test
    public void createPartitionFailed() {
        String partId = "key";
        PartitionInfo newPi = PartitionInfo.builder().build();

        when(partitionServiceCache.get(partId)).thenReturn(null);
        when(partitionServiceImpl.createPartition(partId, newPi)).thenReturn(null);

        cachedPartitionServiceImpl.createPartition(partId, newPi);

        verify(partitionServiceImpl, times(1)).createPartition(partId, newPi);
        verify(partitionServiceCache, times(0)).put(any(), any());
        verify(partitionServiceCache, times(1)).get(any());
    }

    @Test
    public void updatePartitionSucceed() {
        String partId = "key";

        PartitionInfo newPi = PartitionInfo.builder().build();
        PartitionInfo retPi = PartitionInfo.builder().build();

        when(partitionServiceImpl.updatePartition(partId, newPi)).thenReturn(retPi);

        cachedPartitionServiceImpl.updatePartition(partId, newPi);

        verify(partitionServiceImpl, times(1)).updatePartition(partId, newPi);
        verify(partitionServiceCache, times(1)).put(partId, retPi);
    }

    @Test
    public void updatePartitionFailed() {
        String partId = "key";
        PartitionInfo newPi = PartitionInfo.builder().build();

        when(partitionServiceImpl.updatePartition(partId, newPi)).thenReturn(null);

        cachedPartitionServiceImpl.updatePartition(partId, newPi);

        verify(partitionServiceImpl, times(1)).updatePartition(partId, newPi);
        verify(partitionServiceCache, times(0)).put(any(), any());
        verify(partitionServiceCache, times(0)).get(any());
    }

    @Test
    public void getPartition() {
        String partId = "key";

        PartitionInfo retPi = PartitionInfo.builder().build();

        when(partitionServiceImpl.getPartition(partId)).thenReturn(retPi);

        cachedPartitionServiceImpl.getPartition(partId);

        verify(partitionServiceCache, times(1)).get(partId);
        verify(partitionServiceImpl, times(1)).getPartition(partId);
        verify(partitionServiceCache, times(1)).put(partId, retPi);
    }

    @Test
    public void deletePartition() {
        String partId = "key";
        PartitionInfo retPi = PartitionInfo.builder().build();

        when(partitionServiceImpl.deletePartition(partId)).thenReturn(true);
        when(partitionServiceCache.get(partId)).thenReturn(retPi);

        cachedPartitionServiceImpl.deletePartition(partId);

        verify(partitionServiceImpl, times(1)).deletePartition(partId);
        verify(partitionServiceCache, times(1)).delete(partId);
        verify(partitionServiceCache, times(1)).get(partId);
    }

    @Test
    public void getAllPartitions() {
        List<String> partitions = new ArrayList<>();

        when(partitionServiceImpl.getAllPartitions()).thenReturn(partitions);
        cachedPartitionServiceImpl.getAllPartitions();
        String partKey = "getAllPartitions";
        verify(partitionListCache, times(1)).get(partKey);
        verify(partitionServiceImpl, times(1)).getAllPartitions();
        verify(partitionListCache, times(1)).put(partKey, partitions);
    }

}