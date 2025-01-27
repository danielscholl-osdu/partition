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
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.api.descriptor.CreatePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.DeletePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.GetPartitionDescriptor;
import org.opengroup.osdu.partition.util.BaseTestTemplate;
import org.opengroup.osdu.partition.util.TestTokenUtils;
import org.opengroup.osdu.partition.util.TestUtils;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public final class GetPartitionByIdApiTest extends BaseTestTemplate {

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
        DeletePartitionDescriptor deletePartitionDes = new DeletePartitionDescriptor();
        deletePartitionDes.setPartitionId(partitionId);
        deletePartitionDes.run(this.getId(), this.testUtils.getAccessToken());
    }

    @Override
    protected void createResource() throws Exception {
        CreatePartitionDescriptor createPartitionDescriptor = new CreatePartitionDescriptor();
        createPartitionDescriptor.setPartitionId(partitionId);

        CloseableHttpResponse createResponse = createPartitionDescriptor.run(this.getId(), this.testUtils.getAccessToken());
        assertEquals(this.error(EntityUtils.toString(createResponse.getEntity())), HttpStatus.CREATED.value(),
                createResponse.getCode());
    }

    public GetPartitionByIdApiTest() {
        super(new GetPartitionDescriptor());
    }

    @Override
    protected int expectedOkResponseCode() {
        return HttpStatus.OK.value();
    }

    @Test
    public void create_and_read_partition() throws Exception {
        createResource();

        //get partition
        CloseableHttpResponse response = this.descriptor.run(this.getId(), this.testUtils.getAccessToken());
        Map<String, JsonNode> partitionProperties = TestUtils.parseResponse(response);

        assertNotNull(partitionProperties);
        assertEquals(partitionProperties.size(), CreatePartitionDescriptor.getDefaultProperties().size()); // While creating partition, properties are added in CreatePartitionDescriptor#getValidBody
    }
}
