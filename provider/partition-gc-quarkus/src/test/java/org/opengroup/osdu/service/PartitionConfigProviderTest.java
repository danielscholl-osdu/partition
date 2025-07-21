/*
 *  Copyright 2020-2025 Google LLC
 *  Copyright 2020-2025 EPAM Systems, Inc
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opengroup.osdu.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

import io.quarkus.test.junit.QuarkusTest;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.opengroup.osdu.model.exception.AppException;

@QuarkusTest
class PartitionConfigProviderTest {

  private static final String PARTITION_CONFIGS_PATH = "/test/partition/path";
  private static final String ERROR_MESSAGE_CONFIGS_PATH_IS_REQUIRED_BUT_NOT_SET =
      "The environment variable PARTITION_CONFIGS_PATH is required but not set";
  private static final String DOES_NOT_EXIST_MESSAGE_PART = "does not exist";
  private static MockedStatic<Files> filesStaticMock;
  PartitionConfigProvider partitionConfigProvider;

  @BeforeAll
  static void beforeAll() {
    filesStaticMock = mockStatic(Files.class);
  }

  @AfterAll
  static void afterAll() {
    filesStaticMock.close();
  }

  @BeforeEach
  void resetFilesMock() {
    filesStaticMock.reset();
    partitionConfigProvider = new PartitionConfigProvider();
  }

  @Test
  void should_notThrowException_when_fileExists() {
    partitionConfigProvider.setPartitionConfigsPath(PARTITION_CONFIGS_PATH);
    filesStaticMock.when(() -> Files.exists(Paths.get(PARTITION_CONFIGS_PATH))).thenReturn(true);

    assertDoesNotThrow(() -> partitionConfigProvider.init());

    assertEquals(PARTITION_CONFIGS_PATH, partitionConfigProvider.getPartitionConfigsPath());
  }

  @Test
  void should_throwException_when_partitionConfigsPathIsNull() {
    partitionConfigProvider.setPartitionConfigsPath(null);

    AppException exception = assertThrows(AppException.class, () -> partitionConfigProvider.init());

    assertEquals(
        ERROR_MESSAGE_CONFIGS_PATH_IS_REQUIRED_BUT_NOT_SET, exception.getError().getMessage());
  }

  @Test
  void should_throwException_when_partitionConfigsPathIsEmpty() {
    partitionConfigProvider.setPartitionConfigsPath("");

    AppException exception = assertThrows(AppException.class, () -> partitionConfigProvider.init());

    assertEquals(
        ERROR_MESSAGE_CONFIGS_PATH_IS_REQUIRED_BUT_NOT_SET, exception.getError().getMessage());
  }

  @Test
  void should_throwException_when_partitionConfigsPathDoesNotExist() {
    partitionConfigProvider.setPartitionConfigsPath(PARTITION_CONFIGS_PATH);
    filesStaticMock.when(() -> Files.exists(Paths.get(PARTITION_CONFIGS_PATH))).thenReturn(false);

    AppException exception = assertThrows(AppException.class, () -> partitionConfigProvider.init());

    assertTrue(exception.getError().getMessage().contains(DOES_NOT_EXIST_MESSAGE_PART));
  }
}
