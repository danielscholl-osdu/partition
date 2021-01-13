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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.provider.interfaces.IKmsClient;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.gcp.model.PartitionPropertyEntity;
import org.opengroup.osdu.partition.provider.gcp.repository.PartitionPropertyEntityRepository;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartitionServiceImpl implements IPartitionService {

  private static final String UNKNOWN_ERROR_REASON = "unknown error";


  private final PartitionPropertyEntityRepository partitionPropertyEntityRepository;

  private final IKmsClient kmsClient;

  @Override
  public PartitionInfo createPartition(String partitionId, PartitionInfo partitionInfo) {
    if (this.partitionPropertyEntityRepository.findByPartitionId(partitionId).isPresent()) {
      throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, UNKNOWN_ERROR_REASON,
          "Partition already exists.");
    }
    this.partitionPropertyEntityRepository.performTransaction(repository -> {
      for (Map.Entry<String, Property> entry : partitionInfo.getProperties().entrySet()) {
        PartitionPropertyEntity entity = new PartitionPropertyEntity(partitionId,
            entry.getKey(), entry.getValue());
        encryptPartitionPropertyEntityIfNeeded(entity);
        repository.save(entity);
      }
      return true;
    });
    return getEncryptedPartition(partitionId);
  }

  private void encryptPartitionPropertyEntityIfNeeded(PartitionPropertyEntity entity) {
    if (entity.isSensitive()) {
      String propertyValue = entity.getValue().toString();
      try {
        entity.setValue(this.kmsClient.encryptString(propertyValue));
      } catch (IOException e) {
        throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, UNKNOWN_ERROR_REASON,
            e.getMessage());
      }
    }
  }

  private PartitionInfo getEncryptedPartition(String partitionId) {
    List<PartitionPropertyEntity> partitionPropertiesList = this.partitionPropertyEntityRepository
        .findByPartitionId(partitionId)
        .orElseThrow(
            () -> new AppException(HttpStatus.SC_NOT_FOUND, UNKNOWN_ERROR_REASON,
                "Partition does not exist."));
    PartitionInfo partitionInfo = new PartitionInfo();
    Map<String, Property> partitionInfoProperties = new HashMap<>();
    for (PartitionPropertyEntity entity : partitionPropertiesList) {
      partitionInfoProperties
          .put(entity.getName(), new Property(entity.getSensitive(), entity.getValue()));
    }
    partitionInfo.setProperties(partitionInfoProperties);

    return partitionInfo;
  }

  @Transactional
  @Override
  public PartitionInfo updatePartition(String partitionId, PartitionInfo partitionInfo) {
    this.partitionPropertyEntityRepository.findByPartitionId(partitionId)
        .orElseThrow(
            () -> new AppException(HttpStatus.SC_NOT_FOUND, UNKNOWN_ERROR_REASON,
                "An attempt to update not existing partition."));
    for (Map.Entry<String, Property> entry : partitionInfo.getProperties().entrySet()) {
      PartitionPropertyEntity entity = this.partitionPropertyEntityRepository
          .findByPartitionIdAndName(partitionId, entry.getKey());
      if (Objects.nonNull(entity)) {
        entity.setSensitive(entry.getValue().isSensitive());
        entity.setValue(entry.getValue().getValue());
      } else {
        entity = new PartitionPropertyEntity(partitionId, entry.getKey(), entry.getValue());
      }
      encryptPartitionPropertyEntityIfNeeded(entity);
      this.partitionPropertyEntityRepository.save(entity);
    }
    return getEncryptedPartition(partitionId);
  }

  @Transactional
  @Override
  public PartitionInfo getPartition(String partitionId) {
    PartitionInfo partitionInfo = getEncryptedPartition(partitionId);
    for (Property property : partitionInfo.getProperties().values()) {
      decryptPartitionPropertyIfNeeded(property);
    }
    return partitionInfo;
  }

  private void decryptPartitionPropertyIfNeeded(Property property) {
    if (property.isSensitive()) {
      String propertyValue = property.getValue().toString();
      try {
        property.setValue(this.kmsClient.decryptString(propertyValue));
      } catch (IOException e) {
        throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, UNKNOWN_ERROR_REASON,
            e.getMessage());
      }
    }
  }

  @Transactional
  @Override
  public boolean deletePartition(String partitionId) {
    this.partitionPropertyEntityRepository.findByPartitionId(partitionId)
        .orElseThrow(
            () -> new AppException(HttpStatus.SC_NOT_FOUND, UNKNOWN_ERROR_REASON,
                "An attempt to delete not existing partition."));
    this.partitionPropertyEntityRepository.deleteByPartitionId(partitionId);
    return true;
  }

  @Transactional
  @Override
  public List<String> getAllPartitions() {
    List<String> allPartitions = this.partitionPropertyEntityRepository.getAllPartitions();
    return (allPartitions.isEmpty() ? null : allPartitions);
  }
}
