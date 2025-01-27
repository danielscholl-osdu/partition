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

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.api.descriptor.CreatePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.DeletePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.ListPartitionDescriptor;
import org.opengroup.osdu.partition.util.BaseTestTemplate;
import org.opengroup.osdu.partition.util.TestTokenUtils;
import org.opengroup.osdu.partition.util.TestUtils;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class ListPartitionsApiTest extends BaseTestTemplate {

    private String partitionId = getIntegrationTestPrefix() + System.currentTimeMillis();

    @Override
    @Before
    public void setup() {
        this.testUtils = new TestTokenUtils();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        deleteResource();
        this.testUtils = null;
    }

    @Override
    protected String getId() {
        return partitionId;
    }

    @Override
    protected void deleteResource() throws Exception {
        deleteResource(partitionId);
    }

    @Override
    protected void createResource() throws Exception {
        createResource(partitionId);
    }

    private void createResource(String partitionId) throws Exception {
        CreatePartitionDescriptor createPartitionDescriptor = new CreatePartitionDescriptor();
        createPartitionDescriptor.setPartitionId(partitionId);

        CloseableHttpResponse createResponse = createPartitionDescriptor.run(partitionId, this.testUtils.getAccessToken());
        assertEquals(this.error(EntityUtils.toString(createResponse.getEntity())), HttpStatus.CREATED.value(),
                createResponse.getCode());
    }

    private void deleteResource(String partitionId) throws Exception {
        DeletePartitionDescriptor deletePartitionDes = new DeletePartitionDescriptor();
        deletePartitionDes.setPartitionId(partitionId);
        CloseableHttpResponse response = deletePartitionDes.run(partitionId, this.testUtils.getAccessToken());
    }

    public ListPartitionsApiTest() {
        super(new ListPartitionDescriptor());
    }

    @Override
    protected int expectedOkResponseCode() {
        return HttpStatus.OK.value();
    }

    @Test
    public void create_multiple_partitions_and_retrieve_them() throws Exception {
        String partitionId1 = partitionId + "_1";
        String partitionId2 = partitionId + "_2";

        createResource(partitionId1);
        createResource(partitionId2);

        CloseableHttpResponse response = this.descriptor.run(null, this.testUtils.getAccessToken());
        List<String> partitionIds = TestUtils.parseResponse(response);

        deleteResource(partitionId1);
        deleteResource(partitionId2);

        Assert.assertNotNull(partitionIds);
        assertTrue(partitionIds.contains(partitionId1));
        assertTrue(partitionIds.contains(partitionId2));
    }
}
