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

public abstract class RestDescriptor {

    public RestDescriptor() {
    }

    private String arg = "";
    public String arg(){
        return arg;
    }
    public abstract String getPath();
    public abstract String getHttpMethod();
    public abstract String getValidBody();
    public String getQuery() { return ""; }

    public ClientResponse runHttp(String arg, String token) throws Exception{
        this.arg = arg;
        return TestUtils.send(getPath(), getHttpMethod(), token, getValidBody(), getQuery(), true);
    }
    public ClientResponse run(String arg, String token) throws Exception{
        this.arg = arg;
        return TestUtils.send(getPath(), getHttpMethod(), token, getValidBody(), getQuery(), false);
    }
    public ClientResponse runOnCustomerTenant(String arg, String token) throws Exception{
        this.arg = arg;
        return TestUtils.send(getPath(), getHttpMethod(), token, getValidBody(), getQuery(), TestUtils.getCustomerTenantHeaders(), false);
    }
    public ClientResponse runOptions(String arg, String token) throws Exception{
        this.arg = arg;
        return TestUtils.send(getPath(), "OPTIONS", token, "", "", false);
    }

    public ClientResponse runWithCustomPayload(String arg, String body, String token) throws Exception {
        this.arg = arg;
        return TestUtils.send(getPath(), getHttpMethod(), token, body, getQuery(), false);
    }
}
