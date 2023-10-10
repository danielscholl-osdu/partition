/*
  Copyright 2002-2023 Google LLC
  Copyright 2002-2023 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.opengroup.osdu.partition.provider.gcp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.logging.AuditLogger;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.gcp.config.PropertiesConfiguration;
import org.opengroup.osdu.partition.provider.gcp.model.PartitionPropertyEntity;
import org.opengroup.osdu.partition.provider.gcp.osm.repository.OsmPartitionPropertyRepository;

import java.util.*;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PartitionServiceImplTest {

    private static final String PARTITION_ID = "newPartition";
    private static final boolean SENSITIVE = false;
    private static final String NAME = "new-key";
    private static final String VALUE = "new-value";
    public static final String SYSTEM_PARTITION_ID = "systemPartitionId";

    @Mock
    private ICache<String, PartitionInfo> partitionServiceCache;

    @Mock
    private ICache<String, List<String>> partitionListCache;

    @Mock
    private OsmPartitionPropertyRepository partitionPropertyEntityRepository;

    @Mock
    private AuditLogger auditLogger;

    private PartitionServiceImpl partitionServiceImpl;

    private PartitionInfo expectedPartitionInfo;
    private PartitionPropertyEntity partitionPropertyEntity;
    private Optional<List<PartitionPropertyEntity>> partitionPropertyEntityList;
    private Optional<List<PartitionPropertyEntity>> emptyList;

    @Mock
    private PropertiesConfiguration propertiesConfiguration;

    @BeforeEach
    public void setup() {
        partitionServiceImpl = new PartitionServiceImpl(
                partitionPropertyEntityRepository,
                auditLogger,
                partitionServiceCache,
                partitionListCache,
                propertiesConfiguration

        );

        expectedPartitionInfo = new PartitionInfo();

        Property property = new Property();
        property.setSensitive(SENSITIVE);
        property.setValue(VALUE);

        Map<String, Property> properties = new HashMap<>();
        properties.put(NAME, property);

        expectedPartitionInfo.setProperties(properties);

        partitionPropertyEntity = new PartitionPropertyEntity(PARTITION_ID, NAME, property);

        List<PartitionPropertyEntity> entities = new ArrayList<>();
        entities.add(partitionPropertyEntity);
        partitionPropertyEntityList = Optional.of(entities);

        emptyList = Optional.empty();
    }

    @Test
    public void should_createPartition_when_partitionDoesNotExist() {
        when(partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
                .thenReturn(emptyList, partitionPropertyEntityList);

        PartitionInfo actualPartitionInfo = partitionServiceImpl
                .createPartition(PARTITION_ID, expectedPartitionInfo);

        assertEquals(expectedPartitionInfo, actualPartitionInfo);
    }

    @Test
    public void should_throwAnException_when_createPartitionWhichAlreadyExists() {
        assertThrows(AppException.class, () -> {
            when(partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
                    .thenReturn(emptyList);

            partitionServiceImpl.createPartition(PARTITION_ID, new PartitionInfo());
        });
    }

    @Test
    public void should_updatePartition_when_partitionExists() {
        when(partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
                .thenReturn(partitionPropertyEntityList);
        when(partitionPropertyEntityRepository.findByPartitionIdAndName(PARTITION_ID, NAME))
                .thenReturn(null);

        PartitionInfo actualPartitionInfo = partitionServiceImpl
                .updatePartition(PARTITION_ID, expectedPartitionInfo);

        assertEquals(expectedPartitionInfo, actualPartitionInfo);
    }

    @Test
    public void should_throwAnException_when_updatePartitionWhichDoesNotExist() {
        assertThrows(AppException.class, () -> {
            when(partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
                    .thenReturn(emptyList);
            partitionServiceImpl.createPartition(PARTITION_ID, new PartitionInfo());
        });
    }

    @Test
    public void should_getPartition_when_partitionExists() {
        when(partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
                .thenReturn(partitionPropertyEntityList);

        PartitionInfo actualPartitionInfo = partitionServiceImpl.getPartition(PARTITION_ID);

        assertEquals(expectedPartitionInfo, actualPartitionInfo);
    }

    @Test
    public void should_throwAnException_when_getPartitionWhichDoesNotExist() {
        assertThrows(AppException.class, () -> {
            when(partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
                    .thenReturn(emptyList);
            partitionServiceImpl.getPartition(PARTITION_ID);
        });
    }

    @Test
    public void should_deletePartition_when_partitionExists() {
        when(partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
                .thenReturn(partitionPropertyEntityList);
        doNothing().when(partitionPropertyEntityRepository).deleteByPartitionId(PARTITION_ID);

        boolean expected = true;
        boolean actual = partitionServiceImpl.deletePartition(PARTITION_ID);

        assertEquals(expected, actual);
    }

    @Test
    public void should_throwAnException_when_deletePartitionWhichDoesNotExist() {
        assertThrows(AppException.class, () -> {
            when(partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
                    .thenReturn(emptyList);
            partitionServiceImpl.deletePartition(PARTITION_ID);
        });
    }

    @Test
    public void should_getAllPartitions() {
        List<String> expectedPartitions = new ArrayList<>();
        expectedPartitions.add(PARTITION_ID);

        when(partitionPropertyEntityRepository.getAllPartitions()).thenReturn(expectedPartitions);

        List<String> actualPartitions = partitionServiceImpl.getAllPartitions();

        assertEquals(expectedPartitions, actualPartitions);
    }

    @Test
    public void should_getAllPartitionsExceptEmptySystemPartition() {
        var expectedPartitions = new ArrayList<String>();
        expectedPartitions.add(PARTITION_ID);
        expectedPartitions.add(SYSTEM_PARTITION_ID);

        when(propertiesConfiguration.getSystemPartitionId()).thenReturn("");
        when(partitionPropertyEntityRepository.getAllPartitions()).thenReturn(expectedPartitions);

        var actualPartitions = partitionServiceImpl.getAllPartitions();

        assertEquals(expectedPartitions, actualPartitions);
    }

    @Test
    public void should_getAllPartitionsExceptSystemPartition() {
        var expectedPartitions = new ArrayList<String>();
        expectedPartitions.add(PARTITION_ID);
        expectedPartitions.add(SYSTEM_PARTITION_ID);

        when(propertiesConfiguration.getSystemPartitionId()).thenReturn(SYSTEM_PARTITION_ID);
        when(partitionPropertyEntityRepository.getAllPartitions()).thenReturn(expectedPartitions);

        var actualPartitions= partitionServiceImpl.getAllPartitions();

        assertTrue(!actualPartitions.contains(SYSTEM_PARTITION_ID));
    }
}