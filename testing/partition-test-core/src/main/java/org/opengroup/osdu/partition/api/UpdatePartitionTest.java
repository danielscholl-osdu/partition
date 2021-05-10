package org.opengroup.osdu.partition.api;

import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;
import org.opengroup.osdu.partition.api.descriptor.CreatePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.DeletePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.UpdatePartitionDescriptor;
import org.opengroup.osdu.partition.util.BaseTestTemplate;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;

public abstract class UpdatePartitionTest extends BaseTestTemplate {

    protected String partitionId = getIntegrationTestPrefix() + System.currentTimeMillis();

    private String nonExistentPartitionId = "nonexistent-partition"+System.currentTimeMillis();

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

    @Override
    protected int expectedOkResponseCode() {
        return HttpStatus.CREATED.value();
    }

    @Test
    public void should_return404_when_updatingNonExistentPartition() throws Exception {
        ClientResponse response = this.descriptor.run(nonExistentPartitionId, this.testUtils.getAccessToken());
        assertEquals(this.error(""), HttpStatus.NOT_FOUND.value(), (long) response.getStatus());
    }

    @Test
    public void should_return400_when_updatingPartitionWithIdField() throws Exception {
        createResource();
        ClientResponse response = this.descriptor.runWithCustomPayload(this.getId(), getInvalidBodyForUpdatePartition(), this.testUtils.getAccessToken());
        assertEquals(this.error(""), HttpStatus.BAD_REQUEST.value(), (long) response.getStatus());
        deleteResource();
    }

    @Test
    @Override
    public void should_return20XResponseCode_when_makingValidHttpsRequest() throws Exception {
        createResource();
        ClientResponse response = this.descriptor.runWithCustomPayload(this.getId(), getValidBodyForUpdatePartition(), this.testUtils.getAccessToken());
        deleteResource();
        assertEquals(response.getStatus(), HttpStatus.NO_CONTENT.value());
        assertEquals("GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH", response.getHeaders().getFirst("Access-Control-Allow-Methods"));
        assertEquals("access-control-allow-origin, origin, content-type, accept, authorization, data-partition-id, correlation-id, appkey", response.getHeaders().getFirst("Access-Control-Allow-Headers"));
        assertEquals("*", response.getHeaders().getFirst("Access-Control-Allow-Origin"));
        assertEquals("true", response.getHeaders().getFirst("Access-Control-Allow-Credentials"));
        assertEquals("default-src 'self'", response.getHeaders().getFirst("Content-Security-Policy"));
        assertEquals("max-age=31536000; includeSubDomains", response.getHeaders().getFirst("Strict-Transport-Security"));
        assertEquals("0", response.getHeaders().getFirst("Expires"));
        assertEquals("DENY", response.getHeaders().getFirst("X-Frame-Options"));
        assertEquals("private, max-age=300", response.getHeaders().getFirst("Cache-Control"));
        assertEquals("1; mode=block", response.getHeaders().getFirst("X-XSS-Protection"));
        assertEquals("nosniff", response.getHeaders().getFirst("X-Content-Type-Options"));
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