/*
 * Copyright 2020-2025 Google LLC
 * Copyright 2020-2025 EPAM Systems, Inc
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

package org.opengroup.osdu.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Setter
@Getter
@ApplicationScoped
public class PropertyConfiguration {

  @ConfigProperty(name = "system-partition-id", defaultValue = "system")
  private String systemPartitionId;
}
