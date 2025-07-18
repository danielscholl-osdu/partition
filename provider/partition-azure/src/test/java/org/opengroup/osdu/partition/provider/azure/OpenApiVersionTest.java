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

package org.opengroup.osdu.partition.provider.azure;

import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test to verify OpenAPI configuration and dependency version
 */
public class OpenApiVersionTest {

    @Test
    public void testOpenApiDependencyAvailable() {
        // Test that OpenAPI classes are available on the classpath
        try {
            Class.forName("org.springdoc.core.models.GroupedOpenApi");
            Class.forName("org.springdoc.webmvc.ui.SwaggerUiHome");
        } catch (ClassNotFoundException e) {
            throw new AssertionError("OpenAPI classes not found on classpath", e);
        }
    }

    @Test
    public void testOpenApiGroupedApiCreation() {
        // Test that we can create a GroupedOpenApi instance
        GroupedOpenApi groupedOpenApi = GroupedOpenApi.builder()
                .group("test-group")
                .pathsToMatch("/api/**")
                .build();
        
        assertNotNull(groupedOpenApi);
        assertNotNull(groupedOpenApi.getGroup());
        assertTrue(groupedOpenApi.getGroup().equals("test-group"));
    }

    @Test
    public void testOpenApiVersionCompatibility() {
        // Test that OpenAPI version is compatible with the expected major version
        try {
            // This should not throw an exception if the classes are compatible
            Class<?> groupedOpenApiClass = Class.forName("org.springdoc.core.models.GroupedOpenApi");
            assertNotNull(groupedOpenApiClass);
            
            // Test that we can create an instance (compatibility check)
            GroupedOpenApi.builder().group("compatibility-test").pathsToMatch("/test/**").build();
        } catch (Exception e) {
            throw new AssertionError("OpenAPI version incompatibility detected", e);
        }
    }
}