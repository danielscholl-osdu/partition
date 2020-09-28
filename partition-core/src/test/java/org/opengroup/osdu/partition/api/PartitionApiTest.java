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

package org.opengroup.osdu.partition.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PartitionApiTest {

    private final AppException NOT_FOUND_EXCEPTION =
            new AppException(org.apache.http.HttpStatus.SC_NOT_FOUND, "partition not found"
                    , String.format("%s partition not found", "fakePartition"));

    @Mock
    private IPartitionService partitionService;

    @InjectMocks
    private PartitionApi sut;

    @Mock
    private PartitionInfo partitionInfo;

    @Test
    public void should_return201AndPartitionId_when_givenValidPartitionId() {
        String partitionId = "partition1";

        when(partitionService.createPartition(anyString(), any(PartitionInfo.class))).thenReturn(partitionInfo);

        ResponseEntity<PartitionInfo> result = this.sut.create(partitionId, partitionInfo);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(partitionInfo, result.getBody());
    }

    @Test
    public void should_return200AndPartitionProperties_when_gettingPartitionIdSuccessfully() {
        String partitionId = "partition1";
        Map<String, Property> properties = new HashMap<>();

        when(partitionService.getPartition(anyString())).thenReturn(partitionInfo);
        when(partitionInfo.getProperties()).thenReturn(properties);

        ResponseEntity<Map<String, Property>> result = this.sut.get(partitionId);
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(properties, result.getBody());
    }

    @Test
    public void should_returnHttp204_when_deletingPartitionSuccessfully() {
        String partitionId = "partition1";
        ResponseEntity<?> result = this.sut.delete(partitionId);
        assertEquals(HttpStatus.NO_CONTENT.value(), result.getStatusCodeValue());
    }

    @Test(expected = AppException.class)
    public void should_throwException_when_deletingOfNonExistentPartition() {
        when(partitionService.deletePartition(anyString())).thenThrow(NOT_FOUND_EXCEPTION);
        this.sut.delete("fakePartition");
    }

    @Test(expected = AppException.class)
    public void should_throwException_when_deletingNonExistentPartition() {
        when(partitionService.deletePartition(anyString())).thenThrow(NOT_FOUND_EXCEPTION);
        try {
            this.sut.delete("fakePartition");
        }
        catch (AppException ae) {
            assertEquals(HttpStatus.NOT_FOUND.value(), ae.getError().getCode());
            throw ae;
        }
    }
}