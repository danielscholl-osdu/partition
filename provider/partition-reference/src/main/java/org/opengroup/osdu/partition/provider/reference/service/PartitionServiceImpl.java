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

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.provider.interfaces.IKmsClient;
import org.opengroup.osdu.partition.logging.AuditLogger;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.opengroup.osdu.partition.provider.reference.repository.PartitionPropertyEntityRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PartitionServiceImpl implements IPartitionService {

  private static final String UNKNOWN_ERROR_REASON = "unknown error";

  private static final String PARTITION_LIST_KEY = "getAllPartitions";

  private final PartitionPropertyEntityRepository partitionPropertyEntityRepository;

  private final IKmsClient kmsClient;

  private final AuditLogger auditLogger;

  private final ICache<String, PartitionInfo> partitionServiceCache;

  private final ICache<String, List<String>> partitionListCache;

  @Override
  public PartitionInfo createPartition(String partitionId, PartitionInfo partitionInfo) {
    if (partitionServiceCache.get(partitionId) != null)
      throw new AppException(HttpStatus.SC_CONFLICT, "partition exist", "Partition with same id exist");

    if (partitionPropertyEntityRepository.findByPartitionId(partitionId).isPresent()) {
      throw new AppException(HttpStatus.SC_CONFLICT, UNKNOWN_ERROR_REASON,
              "Partition already exists.");
    }
    partitionInfo.getProperties()
            .forEach((key, property) -> encryptPartitionPropertyEntityIfNeeded(property));
    partitionPropertyEntityRepository.createPartition(partitionId, partitionInfo);
    PartitionInfo pi = getPartition(partitionId);

    if (pi != null) {
      partitionServiceCache.put(partitionId, pi);
      partitionListCache.clearAll();
    }

    return pi;
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
    PartitionInfo pi =  getPartition(partitionId);

    if(pi != null) {
      partitionServiceCache.put(partitionId, pi);
    }

    return pi;
  }

  @Override
  public PartitionInfo getPartition(String partitionId) {
    PartitionInfo pi = (PartitionInfo) partitionServiceCache.get(partitionId);

    if (pi == null) {
      Optional<PartitionInfo> result = partitionPropertyEntityRepository.findByPartitionId(
              partitionId);
      if (!result.isPresent()) {
        throw new AppException(HttpStatus.SC_NOT_FOUND, UNKNOWN_ERROR_REASON, "Partition does not exist.");
      }
      result.get().getProperties()
          .forEach((key, property) -> decryptPartitionPropertyIfNeeded(property));
      pi = result.get();
      partitionServiceCache.put(partitionId, pi);
    }
    return pi;
  }

  @Override
  public boolean deletePartition(String partitionId) {
    if (!partitionPropertyEntityRepository.findByPartitionId(partitionId).isPresent()) {
      this.auditLogger.deletePartitionFailure(Collections.singletonList(partitionId));
      throw new AppException(HttpStatus.SC_NOT_FOUND, UNKNOWN_ERROR_REASON,
              "An attempt to delete not existing partition.");
    }
    if (partitionPropertyEntityRepository.isDeletedPartitionInfoByPartitionId(partitionId)) {
      if (partitionServiceCache.get(partitionId) != null) {
        partitionServiceCache.delete(partitionId);
      }
      partitionListCache.clearAll();
      return true;
    }

    return false;
  }

  @Override
  public List<String> getAllPartitions() {
    List<String> partitions = (List<String>)partitionListCache.get(PARTITION_LIST_KEY);

    if (partitions == null) {
      partitions = partitionPropertyEntityRepository.getAllPartitions();

      if (partitions != null) {
        partitionListCache.put(PARTITION_LIST_KEY, partitions);
      }
    }
    return partitions;
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
