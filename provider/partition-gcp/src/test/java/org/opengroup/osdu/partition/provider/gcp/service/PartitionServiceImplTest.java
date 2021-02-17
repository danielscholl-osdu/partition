/*
  Copyright 2002-2021 Google LLC
  Copyright 2002-2021 EPAM Systems, Inc

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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.logging.AuditLogger;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.gcp.model.PartitionPropertyEntity;
import org.opengroup.osdu.partition.provider.gcp.repository.PartitionPropertyEntityRepository;

@RunWith(MockitoJUnitRunner.class)
public class PartitionServiceImplTest {

  private static final String PARTITION_ID = "newPartition";
  private static final boolean SENSITIVE = false;
  private static final String NAME = "new-key";
  private static final String VALUE = "new-value";


  @Mock
  private PartitionPropertyEntityRepository partitionPropertyEntityRepository;

  @Mock
  private AuditLogger auditLogger;

  @InjectMocks
  private PartitionServiceImpl partitionServiceImpl;

  private PartitionInfo expectedPartitionInfo;
  private PartitionPropertyEntity partitionPropertyEntity;
  private Optional<List<PartitionPropertyEntity>> partitionPropertyEntityList;
  private Optional<List<PartitionPropertyEntity>> emptyList;

  @Before
  public void setup() {
    this.expectedPartitionInfo = new PartitionInfo();

    Property property = new Property();
    property.setSensitive(SENSITIVE);
    property.setValue(VALUE);

    Map<String, Property> properties = new HashMap<>();
    properties.put(NAME, property);

    this.expectedPartitionInfo.setProperties(properties);

    partitionPropertyEntity = new PartitionPropertyEntity();
    partitionPropertyEntity.setPartitionId(PARTITION_ID);
    partitionPropertyEntity.setName(NAME);
    partitionPropertyEntity.setSensitive(SENSITIVE);
    partitionPropertyEntity.setValue(VALUE);

    List<PartitionPropertyEntity> entities = new ArrayList<>();
    entities.add(partitionPropertyEntity);
    this.partitionPropertyEntityList = Optional.of(entities);

    this.emptyList = Optional.empty();
  }

  @Test
  public void should_createPartition_when_partitionDoesNotExist() {
    when(this.partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
        .thenReturn(this.emptyList, this.partitionPropertyEntityList);

    PartitionInfo actualPartitionInfo = this.partitionServiceImpl
        .createPartition(PARTITION_ID, this.expectedPartitionInfo);

    assertEquals(this.expectedPartitionInfo, actualPartitionInfo);
  }

  @Test(expected = AppException.class)
  public void should_throwAnException_when_createPartitionWhichAlreadyExists() {
    when(this.partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
        .thenReturn(this.emptyList);

    this.partitionServiceImpl.createPartition(PARTITION_ID, new PartitionInfo());
  }

  @Test
  public void should_updatePartition_when_partitionExists() {
    when(this.partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
        .thenReturn(this.partitionPropertyEntityList);
    when(this.partitionPropertyEntityRepository.findByPartitionIdAndName(PARTITION_ID, NAME))
        .thenReturn(null);

    PartitionInfo actualPartitionInfo = this.partitionServiceImpl
        .updatePartition(PARTITION_ID, this.expectedPartitionInfo);

    assertEquals(this.expectedPartitionInfo, actualPartitionInfo);
  }

  @Test(expected = AppException.class)
  public void should_throwAnException_when_updatePartitionWhichDoesNotExist() {
    when(this.partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
        .thenReturn(this.emptyList);
    this.partitionServiceImpl.createPartition(PARTITION_ID, new PartitionInfo());
  }

  @Test
  public void should_getPartition_when_partitionExists() {
    when(this.partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
        .thenReturn(this.partitionPropertyEntityList);

    PartitionInfo actualPartitionInfo = this.partitionServiceImpl.getPartition(PARTITION_ID);

    assertEquals(this.expectedPartitionInfo, actualPartitionInfo);
  }

  @Test(expected = AppException.class)
  public void should_throwAnException_when_getPartitionWhichDoesNotExist() {
    when(this.partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
        .thenReturn(this.emptyList);
    this.partitionServiceImpl.getPartition(PARTITION_ID);
  }

  @Test
  public void should_deletePartition_when_partitionExists() {
    when(this.partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
        .thenReturn(this.partitionPropertyEntityList);
    doNothing().when(this.partitionPropertyEntityRepository).deleteByPartitionId(PARTITION_ID);

    boolean expected = true;
    boolean actual = partitionServiceImpl.deletePartition(PARTITION_ID);

    assertEquals(expected, actual);
  }

  @Test(expected = AppException.class)
  public void should_throwAnException_when_deletePartitionWhichDoesNotExist() {
    when(this.partitionPropertyEntityRepository.findByPartitionId(PARTITION_ID))
        .thenReturn(this.emptyList);
    this.partitionServiceImpl.deletePartition(PARTITION_ID);
  }

  @Test
  public void should_getAllPartitions() {
    List<String> expectedPartitions = new ArrayList<>();
    expectedPartitions.add(PARTITION_ID);

    when(this.partitionPropertyEntityRepository.getAllPartitions()).thenReturn(expectedPartitions);

    List<String> actualPartitions = this.partitionServiceImpl.getAllPartitions();

    assertEquals(expectedPartitions, actualPartitions);
  }
}