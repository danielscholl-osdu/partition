/*
 * Copyright 2020-2022 Google LLC
 * Copyright 2020-2022 EPAM Systems, Inc
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.api.util.AnthosAuthorizationTestUtil;
import org.opengroup.osdu.partition.util.AnthosTestUtils;

import static org.junit.Assert.assertEquals;

public class TestCreatePartition extends CreatePartitionTest {

    private AnthosAuthorizationTestUtil authorizationTestUtil;

    @Override
    @Before
    public void setup() {
        this.testUtils = new AnthosTestUtils();
        this.authorizationTestUtil = new AnthosAuthorizationTestUtil(this.descriptor, this.testUtils);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        deleteResource();
        this.testUtils = null;
        this.authorizationTestUtil = null;
    }

    @Override
    @Test
    public void should_return40XResponseCode_when_makingRequest_withInvalidPayload()
        throws Exception {
        String invalidPayload = "{\"properties_invalid\":{}}";
        CloseableHttpResponse response = descriptor.runWithCustomPayload(getId(), invalidPayload,
            testUtils.getAccessToken());
        assertEquals(400, response.getCode());
    }

    @Override
    @Test
    public void should_return401_when_noAccessToken() throws Exception {
        authorizationTestUtil.should_return401or403_when_noAccessToken(getId());
    }

    @Override
    @Test
    public void should_return401_when_accessingWithCredentialsWithoutPermission() throws Exception {
        authorizationTestUtil.should_return401or403_when_accessingWithCredentialsWithoutPermission(getId());
    }

    @Override
    @Test
    public void should_return401_when_makingHttpRequestWithoutToken() throws Exception {
        authorizationTestUtil.should_return401or403_when_makingHttpRequestWithoutToken(getId());
    }
}
