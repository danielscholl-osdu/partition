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

package org.opengroup.osdu.partition.provider.reference.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.provider.interfaces.IKmsClient;
import org.opengroup.osdu.partition.logging.AuditLogger;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.opengroup.osdu.partition.provider.reference.repository.PartitionPropertyEntityRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartitionServiceImpl implements IPartitionService {

  private static final String UNKNOWN_ERROR_REASON = "unknown error";

  private final PartitionPropertyEntityRepository partitionPropertyEntityRepository;

  private final IKmsClient kmsClient;

  private final AuditLogger auditLogger;

  @Override
  public PartitionInfo createPartition(String partitionId, PartitionInfo partitionInfo) {
    if (partitionPropertyEntityRepository.findByPartitionId(partitionId).isPresent()) {
      throw new AppException(HttpStatus.SC_CONFLICT, UNKNOWN_ERROR_REASON,
          "Partition already exists.");
    }
    partitionInfo.getProperties()
        .forEach((key, property) -> encryptPartitionPropertyEntityIfNeeded(property));
    partitionPropertyEntityRepository.createPartition(partitionId, partitionInfo);
    return getPartition(partitionId);
  }

  @Override
  public PartitionInfo updatePartition(String partitionId, PartitionInfo partitionInfo) {
    if (partitionInfo.getProperties().containsKey("id")) {
      this.auditLogger.updatePartitionSecretFailure(Collections.singletonList(partitionId));
      throw new AppException(HttpStatus.SC_BAD_REQUEST, "can not update id",
          "the field id can not be updated");
    }
    if (!partitionPropertyEntityRepository.findByPartitionId(partitionId).isPresent()) {
      this.auditLogger.updatePartitionSecretFailure(Collections.singletonList(partitionId));
      throw new AppException(HttpStatus.SC_NOT_FOUND, UNKNOWN_ERROR_REASON,
          "An attempt to update not existing partition.");
    }
    partitionInfo.getProperties().forEach((key, value) -> {
      Optional<Property> property = this.partitionPropertyEntityRepository
          .findByPartitionIdAndName(partitionId, key);
      if (property.isPresent()) {
        property.get().setSensitive(value.isSensitive());
        property.get().setValue(value.getValue());
        encryptPartitionPropertyEntityIfNeeded(property.get());
      }
      encryptPartitionPropertyEntityIfNeeded(value);
    });
    partitionPropertyEntityRepository.updatePartition(partitionId, partitionInfo);
    return getPartition(partitionId);
  }

  @Override
  public PartitionInfo getPartition(String partitionId) {
    Optional<PartitionInfo> result = partitionPropertyEntityRepository.findByPartitionId(
        partitionId);
    if (!result.isPresent()) {
      throw new AppException(HttpStatus.SC_NOT_FOUND, UNKNOWN_ERROR_REASON, "Partition does not exist.");
    }
    result.get().getProperties()
        .forEach((key, property) -> decryptPartitionPropertyIfNeeded(property));
    return result.get();
  }

  @Override
  public boolean deletePartition(String partitionId) {
    if (!partitionPropertyEntityRepository.findByPartitionId(partitionId).isPresent()) {
      this.auditLogger.deletePartitionFailure(Collections.singletonList(partitionId));
      throw new AppException(HttpStatus.SC_NOT_FOUND, UNKNOWN_ERROR_REASON,
          "An attempt to delete not existing partition.");
    }
    return partitionPropertyEntityRepository.isDeletedPartitionInfoByPartitionId(partitionId);
  }

  @Override
  public List<String> getAllPartitions() {
    return partitionPropertyEntityRepository.getAllPartitions();
  }

  private void encryptPartitionPropertyEntityIfNeeded(Property property) {
    if (property.isSensitive()) {
      String propertyValue = property.getValue().toString();
      property.setValue("this.kmsClient.encryptString " + propertyValue);
    }
  }

  private void decryptPartitionPropertyIfNeeded(Property property) {
    if (property.isSensitive()) {
      String propertyValue = property.getValue().toString();
      property.setValue("this.kmsClient.decryptString " + propertyValue);
    }
  }
}
