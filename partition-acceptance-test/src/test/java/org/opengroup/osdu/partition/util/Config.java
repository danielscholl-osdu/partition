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

public class Config {

    public String hostUrl;
    public String osduTenant;
    public String clientTenant;
    public boolean executeAuthorizationDependentTests;

    private static Config config = new Config();

    public static Config Instance() {

        //Note: PARTITION_BASE_URL has a '/' at the end of it
        config.hostUrl = System.getProperty("PARTITION_BASE_URL", System.getenv("PARTITION_BASE_URL"));
        config.clientTenant = System.getProperty("CLIENT_TENANT", System.getenv("CLIENT_TENANT"));
        config.osduTenant = System.getProperty("MY_TENANT", System.getenv("MY_TENANT"));
        config.executeAuthorizationDependentTests = Boolean.parseBoolean(System.getProperty("EXECUTE_AUTHORIZATION_DEPENDENT_TESTS", System.getenv("EXECUTE_AUTHORIZATION_DEPENDENT_TESTS")));
        return config;
    }
}
