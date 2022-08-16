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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.logging.AuditLogger;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ServletUriComponentsBuilder.class)
public class PartitionApiTest {

    private final AppException NOT_FOUND_EXCEPTION =
            new AppException(org.apache.http.HttpStatus.SC_NOT_FOUND, "partition not found"
                    , String.format("%s partition not found", "fakePartition"));

    @Mock
    private IPartitionService partitionService;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private PartitionApi sut;

    @Mock
    private PartitionInfo partitionInfo;

    @Test
    public void should_return201AndPartitionId_when_givenValidPartitionId() {
        String partitionId = "partition1";

        mockStatic(ServletUriComponentsBuilder.class);

        ServletUriComponentsBuilder builder = spy(ServletUriComponentsBuilder.class);

        when(ServletUriComponentsBuilder.fromCurrentRequest()).thenReturn(builder);

        ResponseEntity result = this.sut.create(partitionId, partitionInfo);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNull(result.getBody());
        assertNotNull(result.getHeaders().get(HttpHeaders.LOCATION));
    }

    @Test
    public void should_return204_when_givenUpdatingValidPartitionId() {
        String partitionId = "partition1";
        this.sut.patch(partitionId, partitionInfo);

        ResponseEntity<Map<String, Property>> result = this.sut.get(partitionId);
        assertEquals(HttpStatus.OK, result.getStatusCode());
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

    @Test
    public void should_return200AndListAllPartition() {
        List<String> partitions = new ArrayList<>();
        partitions.add("tenant1");
        partitions.add("tenant2");

        when(partitionService.getAllPartitions()).thenReturn(partitions);

        List<String> result = this.sut.list();
        assertNotNull(result);
        assertEquals(partitions.size(), result.size());
    }
}