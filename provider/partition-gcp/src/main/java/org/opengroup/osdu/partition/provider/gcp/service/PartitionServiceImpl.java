/*
  Copyright 2020 Google LLC
  Copyright 2020 EPAM Systems, Inc

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.gcp.model.PartitionPropertyEntity;
import org.opengroup.osdu.partition.provider.gcp.repository.PartitionPropertyEntityRepository;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartitionServiceImpl implements IPartitionService {

  private static final String UNKNOWN_ERROR_REASON = "unknown error";

  private final PartitionPropertyEntityRepository partitionPropertyEntityRepository;

  @Override
  public PartitionInfo createPartition(String partitionId, PartitionInfo partitionInfo) {
    if (!partitionExists(partitionId)) {
      for (Map.Entry<String, Property> entry : partitionInfo.getProperties().entrySet()) {
        PartitionPropertyEntity partitionPropertyEntity = new PartitionPropertyEntity(partitionId,
            entry.getKey(), entry.getValue());
        partitionPropertyEntityRepository.save(partitionPropertyEntity);
      }
      return getPartition(partitionId);

    } else {
      throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, UNKNOWN_ERROR_REASON,
          "Partition already exists.");
    }

  }

  @Override
  public PartitionInfo updatePartition(String partitionId, PartitionInfo partitionInfo) {
    if (!partitionExists(partitionId)) {
      throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, UNKNOWN_ERROR_REASON,
          "An attempt to update not existing partition.");

    } else {
      for (Map.Entry<String, Property> entry : partitionInfo.getProperties().entrySet()) {
        PartitionPropertyEntity entity = partitionPropertyEntityRepository
            .findByPartitionIdAndName(partitionId, entry.getKey());
        if (entity != null) {
          entity.setSensitive(entry.getValue().isSensitive());
          entity.setValue(entry.getValue().getValue());

        } else {
          entity = new PartitionPropertyEntity(partitionId, entry.getKey(), entry.getValue());
        }
        partitionPropertyEntityRepository.save(entity);
      }
    }
    return getPartition(partitionId);
  }

  @Override
  public PartitionInfo getPartition(String partitionId) {
    if (partitionExists(partitionId)) {
      List<PartitionPropertyEntity> partitionPropertiesList = partitionPropertyEntityRepository
          .findByPartitionId(partitionId);
      PartitionInfo partitionInfo = new PartitionInfo();
      Map<String, Property> partitionInfoProperties = new HashMap<>();
      for (PartitionPropertyEntity entity : partitionPropertiesList) {
        partitionInfoProperties
            .put(entity.getName(), new Property(entity.getSensitive(), entity.getValue()));
      }
      partitionInfo.setProperties(partitionInfoProperties);

      return partitionInfo;

    } else {
      throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, UNKNOWN_ERROR_REASON,
          "Partition does not exist.");
    }

  }

  @Override
  public boolean deletePartition(String partitionId) {
    if (partitionExists(partitionId)) {
      partitionPropertyEntityRepository.deleteByPartitionId(partitionId);
      return true;

    } else {
      throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, UNKNOWN_ERROR_REASON,
          "An attempt to delete not existing partition.");
    }

  }

  @Override
  public List<String> getAllPartitions() {
    Iterable<PartitionPropertyEntity> entities = partitionPropertyEntityRepository.findAll();
    List<String> allPartitions = new ArrayList<>();
    entities.forEach(
        partitionPropertyEntity -> allPartitions.add(partitionPropertyEntity.getPartitionId()));
    List<String> distinctPartitions = allPartitions.stream().distinct()
        .collect(Collectors.toList());
    return distinctPartitions.isEmpty() ? null : distinctPartitions;
  }

  private boolean partitionExists(String partitionId) {
    List<PartitionPropertyEntity> partitionPropertyEntities = partitionPropertyEntityRepository
        .findByPartitionId(partitionId);
    return !partitionPropertyEntities.isEmpty();
  }

}
