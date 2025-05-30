/*
 * Copyright 2020-2024 Google LLC
 * Copyright 2020-2024 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.partition.provider.gcp.controller;

import static org.opengroup.osdu.partition.provider.gcp.config.SystemApiConfiguration.PARTITION_SYSTEM_TENANT_API;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.partition.logging.AuditLogger;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.gcp.api.SystemPartitionApi;
import org.opengroup.osdu.partition.provider.gcp.config.SystemApiConfiguration;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(name = PARTITION_SYSTEM_TENANT_API, havingValue = "true")
public class SystemController implements SystemPartitionApi {

  private final IPartitionService partitionService;
  private final SystemApiConfiguration properties;
  private final AuditLogger auditLogger;

  @Override
  public ResponseEntity<Object> create(PartitionInfo partitionInfo) {
    partitionService.createPartition(properties.getSystemPartitionId(), partitionInfo);
    URI partitionLocation = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand().toUri();
    this.auditLogger.createPartitionSuccess(Collections.singletonList(properties.getSystemPartitionId()));
    return ResponseEntity.created(partitionLocation).build();
  }

  @Override
  public void patch(PartitionInfo partitionInfo) {
    this.partitionService.updatePartition(properties.getSystemPartitionId(), partitionInfo);
    this.auditLogger.updatePartitionSecretSuccess(Collections.singletonList(properties.getSystemPartitionId()));
  }

  @Override
  public ResponseEntity<Map<String, Property>> get() {
    PartitionInfo partitionInfo = this.partitionService.getPartition(properties.getSystemPartitionId());
    this.auditLogger.readPartitionSuccess(Collections.singletonList(properties.getSystemPartitionId()));
    return ResponseEntity.ok(partitionInfo.getProperties());
  }
}
