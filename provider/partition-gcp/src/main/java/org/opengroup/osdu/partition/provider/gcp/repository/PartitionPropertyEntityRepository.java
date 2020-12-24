/*
  Copyright 2020 Google LLC
  Copyright 2020 EPAM Systems, Inc

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

package org.opengroup.osdu.partition.provider.gcp.repository;

import com.google.cloud.datastore.Key;
import java.util.List;
import org.opengroup.osdu.partition.provider.gcp.model.PartitionPropertyEntity;
import org.springframework.cloud.gcp.data.datastore.repository.DatastoreRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartitionPropertyEntityRepository extends
    DatastoreRepository<PartitionPropertyEntity, Key> {

  List<PartitionPropertyEntity> findByPartitionId(String partitionId);

  PartitionPropertyEntity findByName(String partitionId, String name);

  void deleteByPartitionId(String partitionId);

}
