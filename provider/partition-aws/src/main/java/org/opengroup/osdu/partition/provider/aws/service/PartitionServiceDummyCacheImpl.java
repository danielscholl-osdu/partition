// Copyright © 2020 Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.partition.provider.aws.service;

import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionServiceCache;
import org.springframework.stereotype.Service;

/***
 * We don't want to use cache.  Implement a dummy service to always return a cache miss.
 */
@Service
public class PartitionServiceDummyCacheImpl implements IPartitionServiceCache<String, Object> {
    public PartitionServiceDummyCacheImpl() {
        
    }

    @Override
    public void clearAll() {
        return;
    }

    @Override
    public void delete(String arg0) {
        return;
    }

    @Override
    public PartitionInfo get(String arg0) {
        return null;
    }

    @Override
    public void put(String arg0, Object arg1) {
        return;
    }
}
