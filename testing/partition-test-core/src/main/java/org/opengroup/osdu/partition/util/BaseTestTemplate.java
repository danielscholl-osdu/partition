/*
 * Copyright 2017-2020, Schlumberger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.partition.util;

import com.sun.jersey.api.client.ClientResponse;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class BaseTestTemplate extends TestBase {

    protected RestDescriptor descriptor;

    public BaseTestTemplate(RestDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    protected abstract String getId();

    protected abstract void deleteResource() throws Exception;

    protected abstract void createResource() throws Exception;

    protected abstract int expectedOkResponseCode();

    protected String error(String body) {
        return String.format("%s: %s %s %s", descriptor.getHttpMethod(), descriptor.getPath(), descriptor.getQuery(), body);
    }

    protected void validate20XResponse(ClientResponse response, RestDescriptor descriptor) {
        if (response.getStatus() != 204)
            System.out.println(response.getEntity(String.class));
    }

    @Test
    public void should_return401_when_noAccessToken() throws Exception {
        ClientResponse response = descriptor.runOnCustomerTenant(getId(), testUtils.getNoAccessToken());
        assertEquals(error(response.getEntity(String.class)), 401, response.getStatus());
    }

    @Test
    public void should_return401_when_accessingWithCredentialsWithoutPermission() throws Exception {
        ClientResponse response = descriptor.run(getId(), testUtils.getNoAccessToken());
        assertEquals(error(response.getEntity(String.class)), 401, response.getStatus());
    }

    @Test
    public void should_return20XResponseCode_when_makingValidHttpsRequest() throws Exception {
        should_return20X_when_usingCredentialsWithPermission(testUtils.getAccessToken());
    }

    public void should_return20X_when_usingCredentialsWithPermission(String token) throws Exception {
        createResource();
        ClientResponse response = descriptor.run(getId(), token);
        deleteResource();
        assertEquals(error(response.getStatus() == 204 ? "" : response.getEntity(String.class)), expectedOkResponseCode(), response.getStatus());
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

    @Test
    public void should_returnOk_when_makingHttpOptionsRequest() throws Exception {
        createResource();
        ClientResponse response = descriptor.runOptions(getId(), testUtils.getAccessToken());
        assertEquals(error(response.getEntity(String.class)), 200, response.getStatus());
        deleteResource();
    }

    @Test
    public void should_return401_when_makingHttpRequestWithoutToken() throws Exception {
        ClientResponse response = descriptor.run(getId(), "");
        assertEquals(error(response.getEntity(String.class)), 401, response.getStatus());
    }
}
