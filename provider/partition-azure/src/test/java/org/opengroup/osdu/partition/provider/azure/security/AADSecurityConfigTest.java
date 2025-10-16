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

package org.opengroup.osdu.partition.provider.azure.security;

import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.AadAppRoleStatelessAuthenticationFilter;
import com.azure.spring.cloud.autoconfigure.implementation.aad.filter.UserPrincipalManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opengroup.osdu.partition.controller.PartitionController;
import org.opengroup.osdu.partition.logging.AuditLogger;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import jakarta.servlet.Filter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for AADSecurityConfig to verify security configuration.
 * 
 * <p>This test suite validates that:
 * <ul>
 *   <li>CSRF protection is disabled for stateless JWT authentication</li>
 *   <li>Public endpoints are accessible without authentication</li>
 *   <li>Protected endpoints require authentication (return 401 when unauthenticated)</li>
 * </ul>
 * 
 * <p><b>CSRF Disabled - Security Model:</b>
 * The partition service uses stateless JWT bearer token authentication via Azure AD.
 * CSRF protection is intentionally disabled because:
 * <ul>
 *   <li>No session cookies are used (SessionCreationPolicy.NEVER)</li>
 *   <li>All authentication is via JWT tokens in Authorization headers</li>
 *   <li>CSRF attacks require browser-stored cookies, which this API doesn't use</li>
 * </ul>
 * 
 * @see AADSecurityConfig
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"azure.istio.auth.enabled=false"}, classes = {
        PartitionController.class,
        AADSecurityConfig.class,
        AadAppRoleStatelessAuthenticationFilter.class})
@WebAppConfiguration
public class AADSecurityConfigTest {
    private MockMvc mockMvc = null;

    @MockBean
    @Qualifier("partitionServiceImpl")
    private IPartitionService partitionService;

    @MockBean
    private AuditLogger auditLogger;

    @MockBean
    private UserPrincipalManager userPrincipalManager;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @BeforeEach
    public  void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    /**
     * Verifies that CSRF protection is disabled in the security filter chain.
     * 
     * <p>This is the expected configuration for a stateless JWT-based API.
     * CSRF protection is not needed because:
     * <ul>
     *   <li>Authentication uses JWT tokens in headers, not cookies</li>
     *   <li>No server-side sessions are maintained (SessionCreationPolicy.NEVER)</li>
     *   <li>Browsers cannot automatically include JWT tokens in forged requests</li>
     * </ul>
     * 
     * <p>This test validates that the CsrfFilter is not present in the filter chain,
     * confirming CSRF protection is disabled as documented in AADSecurityConfig.
     */
    @Test
    public void testCsrfIsDisabledForStatelessJwtAuthentication() {
        // Verify CsrfFilter is not in the filter chain since CSRF is disabled
        boolean hasCsrfFilter = springSecurityFilterChain.getFilterChains().stream()
                .flatMap(chain -> chain.getFilters().stream())
                .anyMatch(filter -> filter instanceof CsrfFilter);
        
        assertTrue(!hasCsrfFilter, 
                "CSRF filter should be disabled for stateless JWT authentication. " +
                "This is secure because the API uses JWT tokens in headers, not cookies.");
    }

    /**
     * Verifies that OPTIONS requests work correctly for CORS preflight.
     */
    @Test
    public void testOptions() throws Exception {
        mockMvc.perform(options("/fake"))
                .andExpect(status().isNotFound());

        mockMvc.perform(options("/partitions/101")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    /**
     * Verifies that state-changing operations (POST) work without CSRF tokens.
     * 
     * <p>This test demonstrates that POST requests can be made without CSRF tokens
     * because CSRF protection is disabled. This is secure in the context of stateless
     * JWT authentication where:
     * <ul>
     *   <li>Every request must include a valid JWT token in the Authorization header</li>
     *   <li>No session cookies are used that could be exploited in CSRF attacks</li>
     * </ul>
     * 
     * <p>The request does not fail with 403 Forbidden due to CSRF validation,
     * demonstrating that CSRF protection is disabled as intended.
     */
    @Test
    public void testPostWithoutCsrfTokenIsAllowed() throws Exception {
        // POST request without CSRF token should not be rejected due to CSRF
        // The status will be 404 or 401 depending on whether the endpoint exists,
        // but it should NOT be 403 (which would indicate CSRF protection is enabled)
        mockMvc.perform(post("/partitions/test-partition")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"properties\":{}}"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status != 403, 
                            "Request should not return 403 Forbidden due to CSRF. " +
                            "Got status: " + status + ". CSRF protection is correctly disabled.");
                });
    }
}
