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

import static org.junit.Assert.assertEquals;

import com.sun.jersey.api.client.ClientResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.util.AwsTestUtils;


public class TestListPartitions extends ListPartitionsApitTest {

    @Before
    @Override
    public void setup() {
        this.testUtils = new AwsTestUtils();
    }

    @After
    @Override
    public void tearDown() {
        this.testUtils = null;
    }

    @Override
    @Test
    public void should_return20XResponseCode_when_makingValidHttpsRequest() throws Exception {
        createResource();
        ClientResponse response = descriptor.run(getId(), testUtils.getAccessToken());
        deleteResource();
        assertEquals(error(response.getStatus() == 204 ? "" : response.getEntity(String.class)), expectedOkResponseCode(), response.getStatus());
        assertEquals("default-src 'self'", response.getHeaders().getFirst("Content-Security-Policy"));
        assertEquals("max-age=31536000; includeSubDomains", response.getHeaders().getFirst("Strict-Transport-Security"));
        assertEquals("0", response.getHeaders().getFirst("Expires"));
        assertEquals("DENY", response.getHeaders().getFirst("X-Frame-Options"));
        assertEquals("private, max-age=300", response.getHeaders().getFirst("Cache-Control"));
        assertEquals("1; mode=block", response.getHeaders().getFirst("X-XSS-Protection"));
        assertEquals("nosniff", response.getHeaders().getFirst("X-Content-Type-Options"));
    }

}
