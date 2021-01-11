package org.opengroup.osdu.partition.provider.gcp.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.gcp.model.PartitionPropertyEntity;
import org.opengroup.osdu.partition.provider.gcp.repository.PartitionPropertyEntityRepository;

@RunWith(MockitoJUnitRunner.class)
public class PartitionServiceImplTest {

  @Mock
  private PartitionPropertyEntityRepository partitionPropertyEntityRepository;

  @InjectMocks
  private PartitionServiceImpl partitionServiceImpl;

  @Test
  public void should_createPartition_when_partitionDoesNotExist() {
    String partitionId = "newPartition";
    boolean sensitive = false;
    String name = "new-key";
    String value = "new-value";

    PartitionInfo expectedPartitionInfo = new PartitionInfo();
    Property property = new Property();
    property.setSensitive(sensitive);
    property.setValue(value);
    Map<String, Property> properties = new HashMap<>();
    properties.put(name, property);
    expectedPartitionInfo.setProperties(properties);

    when(partitionPropertyEntityRepository.save(any())).thenReturn(new PartitionPropertyEntity());

    PartitionPropertyEntity partitionPropertyEntity = new PartitionPropertyEntity();
    partitionPropertyEntity.setPartitionId(partitionId);
    partitionPropertyEntity.setName(name);
    partitionPropertyEntity.setSensitive(sensitive);
    partitionPropertyEntity.setValue(value);
    List<PartitionPropertyEntity> partitionPropertyEntityList = new ArrayList<>();
    partitionPropertyEntityList.add(partitionPropertyEntity);

    List<PartitionPropertyEntity> emptyList = new ArrayList<>();

    when(partitionPropertyEntityRepository.findByPartitionId(partitionId))
        .thenReturn(emptyList, partitionPropertyEntityList);

    PartitionInfo actualPartitionInfo = partitionServiceImpl
        .createPartition(partitionId, expectedPartitionInfo);

    assertEquals(expectedPartitionInfo, actualPartitionInfo);
  }

  @Test(expected = AppException.class)
  public void should_throwAnException_when_createPartitionWhichAlreadyExists() {
    String partitionId = "newPartition";
    PartitionInfo partitionInfo = new PartitionInfo();
    List<PartitionPropertyEntity> emptyList = new ArrayList<>();
    when(partitionPropertyEntityRepository.findByPartitionId(partitionId)).thenReturn(emptyList);
    partitionServiceImpl.createPartition(partitionId, partitionInfo);
  }

  @Test
  public void should_updatePartition_when_partitionExists() {

  }

  @Test(expected = AppException.class)
  public void should_throwAnException_when_updatePartitionWhichDoesNotExist() {
    String partitionId = "newPartition";
    PartitionInfo partitionInfo = new PartitionInfo();
    List<PartitionPropertyEntity> emptyList = new ArrayList<>();
    when(partitionPropertyEntityRepository.findByPartitionId(partitionId)).thenReturn(emptyList);
    partitionServiceImpl.createPartition(partitionId, partitionInfo);
  }

  @Test
  public void should_getPartition_when_partitionExists() {
  }

  @Test(expected = AppException.class)
  public void should_throwAnException_when_getPartitionWhichDoesNotExist() {
    String partitionId = "newPartition";
    List<PartitionPropertyEntity> emptyList = new ArrayList<>();
    when(partitionPropertyEntityRepository.findByPartitionId(partitionId)).thenReturn(emptyList);
    partitionServiceImpl.getPartition(partitionId);
  }

  @Test
  public void should_deletePartition_when_partitionExists() {
  }

  @Test(expected = AppException.class)
  public void should_throwAnException_when_deletePartitionWhichDoesNotExist() {
    String partitionId = "newPartition";
    List<PartitionPropertyEntity> emptyList = new ArrayList<>();
    when(partitionPropertyEntityRepository.findByPartitionId(partitionId)).thenReturn(emptyList);
    partitionServiceImpl.deletePartition(partitionId);
  }

  @Test
  public void should_getAllPartitions() {
  }
}