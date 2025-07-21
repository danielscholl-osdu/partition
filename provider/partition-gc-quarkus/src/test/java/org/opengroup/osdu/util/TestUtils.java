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

package org.opengroup.osdu.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TestUtils {

  private static final List<String> TEST_FILENAMES =
      List.of("osdu.json", "second.json", "not_valid.json", "not.json.txt", "not_a_partition.json");

  private static final String TEST_FILES_PATH = "/testFiles/";

  public static void resetFilesForTest(Path dir) {
    removeAllFilesInDir(dir);
    try {
      for (String filename : TEST_FILENAMES) {
        copyTestFileToDir(filename, dir);
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not copy test file to temp dir", e);
    }
  }

  public static void removeAllFilesInDir(Path dir) {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
      for (Path entry : stream) {
        if (Files.isRegularFile(entry)) {
          Files.delete(entry);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not delete files in dir " + dir, e);
    }
  }

  private static void copyTestFileToDir(String testFileName, Path dir) throws IOException {
    try (InputStream is =
        EnvVarResource.class.getClassLoader().getResourceAsStream(TEST_FILES_PATH + testFileName)) {
      if (is == null) throw new IllegalArgumentException("Test file not found: " + testFileName);
      Files.copy(is, dir.resolve(testFileName));
    }
  }
}
