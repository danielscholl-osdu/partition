// Copyright 2017-2020, Schlumberger
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
import org.opengroup.osdu.partition.api.descriptor.CreatePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.DeletePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.GetPartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.ListPartitionDescriptor;
import org.opengroup.osdu.partition.util.BaseTestTemplate;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;

public abstract class ListPartitionsApitTest extends BaseTestTemplate {

    private String partitionId = getIntegrationTestPrefix() + System.currentTimeMillis();

    @Override
    protected String getId() {
        return partitionId;
    }

    @Override
    protected void deleteResource() throws Exception {
        DeletePartitionDescriptor deletePartitionDes = new DeletePartitionDescriptor();
        deletePartitionDes.setPartitionId(partitionId);
        ClientResponse response = deletePartitionDes.run(this.getId(), this.testUtils.getAccessToken());
        assertEquals(this.error(""), HttpStatus.NO_CONTENT.value(), (long) response.getStatus());
    }

    @Override
    protected void createResource() throws Exception {
        CreatePartitionDescriptor createPartitionDescriptor = new CreatePartitionDescriptor();
        createPartitionDescriptor.setPartitionId(partitionId);

        ClientResponse createResponse = createPartitionDescriptor.run(this.getId(), this.testUtils.getAccessToken());
        assertEquals(this.error((String) createResponse.getEntity(String.class))
                , HttpStatus.CREATED.value(), (long) createResponse.getStatus());
    }

    public ListPartitionsApitTest() {
        super(new ListPartitionDescriptor());
    }

    @Override
    protected int expectedOkResponseCode() {
        return HttpStatus.OK.value();
    }
}
