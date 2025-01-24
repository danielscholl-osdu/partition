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
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.api.descriptor.CreatePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.DeletePartitionDescriptor;
import org.opengroup.osdu.partition.api.descriptor.ListPartitionDescriptor;
import org.opengroup.osdu.partition.api.util.AuthorizationTestUtil;
import org.opengroup.osdu.partition.util.BaseTestTemplate;
import org.opengroup.osdu.partition.util.Constants;
import org.opengroup.osdu.partition.util.TestTokenUtils;
import org.springframework.http.HttpStatus;

import static org.junit.Assert.assertEquals;

public final class ListPartitionsApiTest extends BaseTestTemplate {

    private String partitionId = getIntegrationTestPrefix() + System.currentTimeMillis();

    @Override
    @Before
    public void setup() {
        this.testUtils = new TestTokenUtils();
        this.authorizationTestUtil = new AuthorizationTestUtil(this.descriptor, this.testUtils);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        deleteResource();
        this.testUtils = null;
        this.authorizationTestUtil = null;
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

    public ListPartitionsApiTest() {
        super(new ListPartitionDescriptor());
    }

    @Override
    protected int expectedOkResponseCode() {
        return HttpStatus.OK.value();
    }

    @Override
    @Test
    public void should_return401_when_noAccessToken() throws Exception {
        Assume.assumeTrue(Constants.EXECUTE_AUTHORIZATION_DEPENDENT_TESTS);
        authorizationTestUtil.should_return401or403_when_noAccessToken(getId());
    }

    @Override
    @Test
    public void should_return401_when_accessingWithCredentialsWithoutPermission() throws Exception {
        Assume.assumeTrue(Constants.EXECUTE_AUTHORIZATION_DEPENDENT_TESTS);
        authorizationTestUtil.should_return401or403_when_accessingWithCredentialsWithoutPermission(getId());
    }

    @Override
    @Test
    public void should_return401_when_makingHttpRequestWithoutToken() throws Exception {
        Assume.assumeTrue(Constants.EXECUTE_AUTHORIZATION_DEPENDENT_TESTS);
        authorizationTestUtil.should_return401or403_when_makingHttpRequestWithoutToken(getId());
    }
}
