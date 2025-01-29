/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
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

package org.opengroup.osdu.partition.api;

import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.api.descriptor.CreatePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.DeletePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.UpdatePartitionDescriptor;
import org.opengroup.osdu.partition.util.BaseTestTemplate;
import org.opengroup.osdu.partition.util.TestTokenUtils;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;

public final class UpdatePartitionTest extends BaseTestTemplate {

    String partitionId = getIntegrationTestPrefix() + System.currentTimeMillis();
    private String nonExistentPartitionId = "nonexistent-partition" + System.currentTimeMillis();

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

    public UpdatePartitionTest() {
        super(new UpdatePartitionDescriptor());
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

    @Override
    protected void createResource() throws Exception {
        CreatePartitionDescriptor createPartitionDescriptor = new CreatePartitionDescriptor();
        createPartitionDescriptor.setPartitionId(partitionId);

        CloseableHttpResponse createResponse = createPartitionDescriptor.run(this.getId(), this.testUtils.getAccessToken());
        assertEquals(this.error(EntityUtils.toString(createResponse.getEntity())), HttpStatus.CREATED.value(),
                createResponse.getCode());
    }

    @Override
    protected int expectedOkResponseCode() {
        return HttpStatus.CREATED.value();
    }

    @Test
    public void should_return404_when_updatingNonExistentPartition() throws Exception {
        CloseableHttpResponse response = this.descriptor.run(nonExistentPartitionId, this.testUtils.getAccessToken());
        assertEquals(this.error(""), HttpStatus.NOT_FOUND.value(), response.getCode());
    }

    @Test
    public void should_return400_when_updatingPartitionWithIdField() throws Exception {
        createResource();
        CloseableHttpResponse response = this.descriptor.runWithCustomPayload(this.getId(), getInvalidBodyForUpdatePartition(), this.testUtils.getAccessToken());
        assertEquals(this.error(""), HttpStatus.BAD_REQUEST.value(), response.getCode());
    }

    @Test
    @Override
    public void should_return20XResponseCode_when_makingValidHttpsRequest() throws Exception {
        createResource();
        CloseableHttpResponse response = this.descriptor.runWithCustomPayload(this.getId(), getValidBodyForUpdatePartition(), this.testUtils.getAccessToken());
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getCode());
    }

    private String getInvalidBodyForUpdatePartition() {
        StringBuffer sb = new StringBuffer();
        sb.append("{\n");
        sb.append("  \"properties\": {")
                .append("\"elasticPassword\": {\"sensitive\":true,\"value\":\"test-password\"},")
                .append("\"serviceBusConnection\": {\"sensitive\":true,\"value\":\"test-service-bus-connection\"},")
                .append("\"complianceRuleSet\": {\"value\":\"shared\"},")
                .append("\"id\": {\"value\":\"test-id\"}")
                .append("}\n")
                .append("}");
        return sb.toString();
    }

    protected String getValidBodyForUpdatePartition() {
        StringBuffer sb = new StringBuffer();
        sb.append("{\n");
        sb.append("  \"properties\": {")
                .append("\"updateElasticPassword\": {\"sensitive\":true,\"value\":\"test-password\"},")
                .append("\"serviceBusConnection\": {\"sensitive\":true,\"value\":\"test-service-bus-connection-update\"},")
                .append("\"complianceRuleSet\": {\"value\":\"shared\"}")
                .append("}\n")
                .append("}");
        return sb.toString();
    }
}
