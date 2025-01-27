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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.api.descriptor.CreatePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.DeletePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.GetPartitionDescriptor;
import org.opengroup.osdu.partition.util.BaseTestTemplate;
import org.opengroup.osdu.partition.util.RestDescriptor;
import org.opengroup.osdu.partition.util.TestTokenUtils;
import org.opengroup.osdu.partition.util.TestUtils;
import org.springframework.http.HttpStatus;

import java.util.Map;

public final class DeletePartitionTest extends BaseTestTemplate {

    private String partitionId;

    private static String integrationTestPrefix = getIntegrationTestPrefix();
    private GetPartitionDescriptor getDescriptor = new GetPartitionDescriptor();

    public DeletePartitionTest() {
        super(createDeleteDescriptor(integrationTestPrefix + System.currentTimeMillis()));
        this.partitionId = ((DeletePartitionDescriptor) this.descriptor).getPartitionId();
    }

    @Before
    @Override
    public void setup() throws Exception {
        this.testUtils = new TestTokenUtils();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        deleteResource();
        this.testUtils = null;
    }

    private static DeletePartitionDescriptor createDeleteDescriptor(String id) {
        DeletePartitionDescriptor deletePartition = new DeletePartitionDescriptor();
        deletePartition.setPartitionId(id);

        return deletePartition;
    }

    @Override
    protected String getId() {
        return partitionId;
    }

    @Override
    protected void deleteResource() throws Exception {
        DeletePartitionDescriptor deletePartitionDes = new DeletePartitionDescriptor();
        deletePartitionDes.setPartitionId(partitionId);
        CloseableHttpResponse response = deletePartitionDes.run(this.getId(), this.testUtils.getAccessToken());
    }

    protected void createResource() throws Exception {
        CreatePartitionDescriptor createPartition = new CreatePartitionDescriptor();

        createPartition.setPartitionId(partitionId);

        RestDescriptor oldDescriptor = this.descriptor;

        this.descriptor = createPartition;

        CloseableHttpResponse createResponse = this.descriptor.run(this.getId(), this.testUtils.getAccessToken());
        Assert.assertEquals(this.error(EntityUtils.toString(createResponse.getEntity())), HttpStatus.CREATED.value(),
                createResponse.getCode());

        this.descriptor = oldDescriptor;
    }

    @Test
    public void should_return404_when_deletingNonExistedPartition() throws Exception {
        CloseableHttpResponse response1 = this.descriptor.run(this.getId(), this.testUtils.getAccessToken());
        Assert.assertEquals(this.error(""), HttpStatus.NOT_FOUND.value(), response1.getCode());
    }

    @Test
    public void create_and_delete_partition() throws Exception {
        //create Resource
        createResource();

        //get partition
        CloseableHttpResponse getPartitionResponse = getDescriptor.run(this.getId(), this.testUtils.getAccessToken());
        Map<String, JsonNode> partitionProperties = TestUtils.parseResponse(getPartitionResponse);
        Assert.assertEquals(HttpStatus.OK.value(), getPartitionResponse.getCode());
        Assert.assertNotNull(partitionProperties);

        //delete partition
        deleteResource();

        //get deleted Partition
        CloseableHttpResponse postDeleteResponse = getDescriptor.run(this.getId(), this.testUtils.getAccessToken());
        Assert.assertEquals(HttpStatus.NOT_FOUND.value(), postDeleteResponse.getCode());
    }

    @Override
    protected int expectedOkResponseCode() {
        return HttpStatus.NO_CONTENT.value();
    }
}
