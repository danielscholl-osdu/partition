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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.partition.logging.AuditLogger;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.gcp.config.SystemApiConfiguration;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
public class SystemControllerTest {

  private static final String SYSTEM_TENANT_ID = "systemTenantId";

  @Mock
  private IPartitionService partitionService;

  @Mock
  private SystemApiConfiguration properties;

  @Mock
  private AuditLogger auditLogger;

  @Mock
  private PartitionInfo partitionInfo;

  @InjectMocks
  private SystemController systemController;

  @Test
  public void should_createPartition_and_logSuccess_when_createCalled() {
    Mockito.when(properties.getSystemPartitionId()).thenReturn(SYSTEM_TENANT_ID);

    MockedStatic<ServletUriComponentsBuilder> mockedUriBuilder = Mockito.mockStatic(ServletUriComponentsBuilder.class);
    ServletUriComponentsBuilder uriBuilder = Mockito.spy(ServletUriComponentsBuilder.class);
    Mockito.when(ServletUriComponentsBuilder.fromCurrentRequest()).thenReturn(uriBuilder);

    ResponseEntity result = systemController.create(partitionInfo);

    Mockito.verify(partitionService).createPartition(Mockito.anyString(), Mockito.any(PartitionInfo.class));
    Mockito.verify(auditLogger).createPartitionSuccess(Collections.singletonList(SYSTEM_TENANT_ID));
    assertEquals(HttpStatus.CREATED, result.getStatusCode());

    mockedUriBuilder.close();
  }

  @Test
  public void should_updatePartition_and_logSuccess_when_patchCalled() {
    Mockito.when(properties.getSystemPartitionId()).thenReturn(SYSTEM_TENANT_ID);

    systemController.patch(partitionInfo);

    Mockito.verify(partitionService).updatePartition(Mockito.anyString(), Mockito.any(PartitionInfo.class));
    Mockito.verify(auditLogger).updatePartitionSecretSuccess(Collections.singletonList(SYSTEM_TENANT_ID));
  }

  @Test
  public void should_getPartition_and_logSuccess_when_getCalled() {
    Mockito.when(properties.getSystemPartitionId()).thenReturn(SYSTEM_TENANT_ID);
    Mockito.when(partitionService.getPartition(Mockito.anyString())).thenReturn(partitionInfo);

    ResponseEntity result = systemController.get();

    Mockito.verify(partitionService).getPartition(Mockito.anyString());
    Mockito.verify(auditLogger).readPartitionSuccess(Collections.singletonList(SYSTEM_TENANT_ID));
    assertEquals(HttpStatus.OK, result.getStatusCode());
  }
}
