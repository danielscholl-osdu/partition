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
import static org.opengroup.osdu.util.TestUtils.resetFilesForTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.opengroup.osdu.model.PartitionInfo;
import org.opengroup.osdu.model.exception.AppException;

@QuarkusTest
class PartitionFileLoaderServiceTest {

  private static final String TEST_PARTITION_1 = "osdu";
  private static final String TEST_PARTITION_2 = "second";
  private static final String NON_EXISTENT_DIRECTORY = "non/existent/directory";
  private static final String TEST_PARTITION_FILE_PATH = "/testFiles/%s.json";
  @Inject PartitionFileLoaderService partitionFileLoaderService;

  @Inject ObjectMapper objectMapper;

  @Test
  void should_loadPartitionsFromValidFiles_and_skipNotValidFiles_when_directoryWithFilesExists(
      @TempDir Path tempDir) throws Exception {
    resetFilesForTest(tempDir);
    Map<String, PartitionInfo> partitionInfoMap =
        partitionFileLoaderService.loadPartitionInfoMapFromFiles(tempDir.toString());

    List<String> validPartitions = List.of(TEST_PARTITION_1, TEST_PARTITION_2);
    assertEquals(validPartitions.size(), partitionInfoMap.size());
    for (String partition : validPartitions) {
      assertTrue(partitionInfoMap.containsKey(partition));

      JsonNode expectedProperties =
          objectMapper.readTree(
              getClass().getResourceAsStream(TEST_PARTITION_FILE_PATH.formatted(partition)));
      JsonNode actualProperties =
          objectMapper.valueToTree(partitionInfoMap.get(partition).getProperties());
      assertEquals(expectedProperties, actualProperties);
    }
  }

  @Test
  void should_loadEmptyPartitionInfoMap_when_emptyDirectoryExists(@TempDir Path emptyTempDir) {
    Map<String, PartitionInfo> partitionInfoMap =
        partitionFileLoaderService.loadPartitionInfoMapFromFiles(emptyTempDir.toString());

    assertEquals(0, partitionInfoMap.size());
  }

  @Test
  void should_throwAppException_when_directoryDoesNotExist() {
    AppException exception =
        assertThrows(
            org.opengroup.osdu.model.exception.AppException.class,
            () -> partitionFileLoaderService.loadPartitionInfoMapFromFiles(NON_EXISTENT_DIRECTORY));
    assertEquals(500, exception.getError().getCode());
  }
}
