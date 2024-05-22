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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
class PartitionServiceImplTest {

  private static final String PARTITION_ID = "newPartition";
  private static final boolean SENSITIVE = false;
  private static final String NAME = "new-key";
  private static final String VALUE = "new-value";
  static final String SYSTEM_PARTITION_ID = "systemPartitionId";

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
  void setup() {
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
  void should_createPartition_when_partitionDoesNotExist() {
    when(partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
        .thenReturn(emptyList, partitionPropertyEntityList);

    PartitionInfo actualPartitionInfo = partitionServiceImpl
        .createPartition(PARTITION_ID, expectedPartitionInfo);

    assertEquals(expectedPartitionInfo, actualPartitionInfo);
  }

  @Test
  void should_throwAnException_when_createPartitionWhichAlreadyExists() {
    when(partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID)).thenReturn(emptyList);
     PartitionInfo partitionInfo = new PartitionInfo();
    assertThrows(AppException.class, () -> {
       partitionServiceImpl.createPartition(PARTITION_ID, partitionInfo);
    });
  }

  @Test
  void should_updatePartition_when_partitionExists() {
    when(partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
        .thenReturn(partitionPropertyEntityList);
    when(partitionPropertyEntityRepository.findByPartitionIdAndName(PARTITION_ID, NAME))
        .thenReturn(null);

    PartitionInfo actualPartitionInfo = partitionServiceImpl
        .updatePartition(PARTITION_ID, expectedPartitionInfo);

    assertEquals(expectedPartitionInfo, actualPartitionInfo);
  }

  @Test
  void should_throwAnException_when_updatePartitionWhichDoesNotExist() {
    when(partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID)).thenReturn(emptyList);
     PartitionInfo partitionInfo = new PartitionInfo();
     assertThrows(AppException.class, () -> {
      partitionServiceImpl.createPartition(PARTITION_ID, partitionInfo);
    });
  }

  @Test
  void should_getPartition_when_partitionExists() {
    when(partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
        .thenReturn(partitionPropertyEntityList);

    PartitionInfo actualPartitionInfo = partitionServiceImpl.getPartition(PARTITION_ID);

    assertEquals(expectedPartitionInfo, actualPartitionInfo);
  }

  @Test
  void should_throwAnException_when_getPartitionWhichDoesNotExist() {
    when(partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID)).thenReturn(emptyList);
    assertThrows(AppException.class, () -> {
      partitionServiceImpl.getPartition(PARTITION_ID);
    });
  }

  @Test
  void should_deletePartition_when_partitionExists() {
    when(partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
        .thenReturn(partitionPropertyEntityList);
    doNothing().when(partitionPropertyEntityRepository).deleteByPartitionId(PARTITION_ID);

    boolean expected = true;
    boolean actual = partitionServiceImpl.deletePartition(PARTITION_ID);

    assertEquals(expected, actual);
  }

  @Test
  void should_throwAnException_when_deletePartitionWhichDoesNotExist() {
    when(partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID)).thenReturn(emptyList);
    assertThrows(AppException.class, () -> {
      partitionServiceImpl.deletePartition(PARTITION_ID);
    });
  }

  @Test
  void should_getAllPartitions() {
    List<String> expectedPartitions = new ArrayList<>();
    expectedPartitions.add(PARTITION_ID);

    when(partitionPropertyEntityRepository.getAllPartitions()).thenReturn(expectedPartitions);

    List<String> actualPartitions = partitionServiceImpl.getAllPartitions();

    assertEquals(expectedPartitions, actualPartitions);
  }
}
