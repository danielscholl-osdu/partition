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

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.logging.AuditLogger;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.gcp.config.SystemApiConfiguration;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
public class PartitionControllerV2Test {

  private static final String SYSTEM_TENANT_ID = "systemTenantId";
  private static final String PARTITION_ID_1 = "partition1";
  private static final String PARTITION_ID_2 = "partition2";
  private final AppException FORBIDDEN_EXCEPTION = new AppException(SC_FORBIDDEN, "Not allowed.",
      "The system tenant should be managed via system tenant API.");

  @Mock
  private SystemApiConfiguration properties;

  @Mock
  private PartitionInfo partitionInfo;

  @Mock
  private IPartitionService partitionService;

  @Mock
  private AuditLogger auditLogger;

  @InjectMocks
  private PartitionControllerV2 sut;

  @Test
  public void should_throwAppException_when_createWithSystemTenantId() {
    when(this.properties.getSystemPartitionId()).thenReturn(SYSTEM_TENANT_ID);
    try {
      ResponseEntity result = sut.create(SYSTEM_TENANT_ID, partitionInfo);
      fail("Should have thrown an exception");
    } catch (AppException e) {
      assertEquals(FORBIDDEN_EXCEPTION, e);
    }
  }

  @Test
  public void should_return201AndPartitionId_when_givenValidPartitionId() {
    try (MockedStatic<ServletUriComponentsBuilder> mockedSettings = mockStatic(ServletUriComponentsBuilder.class)) {
      ServletUriComponentsBuilder builder = spy(ServletUriComponentsBuilder.class);
      when(ServletUriComponentsBuilder.fromCurrentRequest()).thenReturn(builder);

      ResponseEntity result = this.sut.create(PARTITION_ID_1, partitionInfo);
      assertEquals(HttpStatus.CREATED, result.getStatusCode());
      assertNull(result.getBody());
      assertNotNull(result.getHeaders().get(HttpHeaders.LOCATION));
    }
  }

  @Test
  public void should_throwAppException_when_patchWithSystemTenantId() {
    when(this.properties.getSystemPartitionId()).thenReturn(SYSTEM_TENANT_ID);
    try {
      sut.patch(SYSTEM_TENANT_ID, partitionInfo);
      fail("Should have thrown an exception");
    } catch (AppException e) {
      assertEquals(FORBIDDEN_EXCEPTION, e);
    }
  }

  @Test
  public void should_return204_when_givenUpdatingValidPartitionId() {
    Map<String, Property> properties = new HashMap<>();

    when(partitionService.getPartition(PARTITION_ID_1)).thenReturn(partitionInfo);
    when(partitionInfo.getProperties()).thenReturn(properties);

    this.sut.patch(PARTITION_ID_1, partitionInfo);

    ResponseEntity<Map<String, Property>> result = this.sut.get(PARTITION_ID_1);
    assertEquals(HttpStatus.OK, result.getStatusCode());
  }

  @Test
  public void should_return200AndPartitionProperties_when_gettingPartitionIdSuccessfully() {
    Map<String, Property> properties = new HashMap<>();

    when(partitionService.getPartition(anyString())).thenReturn(partitionInfo);
    when(partitionInfo.getProperties()).thenReturn(properties);

    ResponseEntity<Map<String, Property>> result = this.sut.get(PARTITION_ID_1);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(properties, result.getBody());
  }

  @Test
  public void should_throwAppException_when_deleteWithSystemTenantId() {
    when(this.properties.getSystemPartitionId()).thenReturn(SYSTEM_TENANT_ID);
    try {
      ResponseEntity result = sut.delete(SYSTEM_TENANT_ID);
      fail("Should have thrown an exception");
    } catch (AppException e) {
      assertEquals(FORBIDDEN_EXCEPTION, e);
    }
  }

  @Test
  public void should_returnHttp204_when_deletingPartitionSuccessfully() {
    ResponseEntity<?> result = this.sut.delete(PARTITION_ID_1);
    assertEquals(HttpStatus.NO_CONTENT.value(), result.getStatusCode().value());
  }

  @Test
  public void should_notContainSystemTenantId_when_list() {
    when(this.properties.getSystemPartitionId()).thenReturn(SYSTEM_TENANT_ID);
    when(properties.isSystemPartitionListableAndResourceReady()).thenReturn(false);
    List<String> partitions = Arrays.asList(PARTITION_ID_1, PARTITION_ID_2, SYSTEM_TENANT_ID);
    when(partitionService.getAllPartitions()).thenReturn(partitions);
    assertFalse(sut.list().contains(SYSTEM_TENANT_ID));
  }

  @Test
  public void should_ContainSystemTenantId_when_list_enabled() {
    when(properties.isSystemPartitionListableAndResourceReady()).thenReturn(true);
    List<String> partitions = Arrays.asList(PARTITION_ID_1, PARTITION_ID_2, SYSTEM_TENANT_ID);
    when(partitionService.getAllPartitions()).thenReturn(partitions);
    assertTrue(sut.list().contains(SYSTEM_TENANT_ID));
  }

  @Test
  public void should_return200AndListAllPartition() {
    List<String> partitions = List.of(PARTITION_ID_1, PARTITION_ID_2);

    when(partitionService.getAllPartitions()).thenReturn(partitions);

    List<String> result = this.sut.list();
    assertEquals(partitions.size(), result.size());
  }
}
