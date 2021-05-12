// Copyright © 2021 Amazon Web Services
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

package org.opengroup.osdu.partition.api;

import com.sun.jersey.api.client.ClientResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.api.descriptor.DeletePartitionDescriptor;
import org.opengroup.osdu.partition.util.AwsTestUtils;

public class TestUpdatePartition extends UpdatePartitionTest {

    @Before
    @Override
    public void setup() {
        this.testUtils = new AwsTestUtils();
    }

    @After
    @Override
    public void tearDown() {
        try {
            this.deleteResource();
        }
        catch (Exception e) {
            
        }
        this.testUtils = null;
    }

    @Override
    protected void deleteResource() throws Exception {
        DeletePartitionDescriptor deletePartitionDes = new DeletePartitionDescriptor();
        deletePartitionDes.setPartitionId(partitionId);
        ClientResponse response = deletePartitionDes.run(this.getId(), this.testUtils.getAccessToken());        
    }

}
