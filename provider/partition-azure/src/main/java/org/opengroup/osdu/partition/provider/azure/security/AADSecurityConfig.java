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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

/**
 * Azure Active Directory Security Configuration for Partition Service.
 * 
 * <p>This configuration implements stateless JWT-based authentication using Azure AD tokens.
 * It is activated when Istio authentication is disabled (azure.istio.auth.enabled=false).
 * 
 * <h3>CSRF Protection Disabled - Justification:</h3>
 * <p>CSRF (Cross-Site Request Forgery) protection is intentionally disabled in this configuration
 * because this is a stateless REST API that uses JWT bearer token authentication. CSRF protection
 * is not necessary in this context for the following reasons:
 * 
 * <ul>
 *   <li><b>Stateless Authentication:</b> The API uses JWT tokens passed in the Authorization header,
 *       not session cookies. CSRF attacks exploit browser-stored cookies, which are not used here.</li>
 *   <li><b>No Session State:</b> Session management is set to NEVER, meaning no server-side sessions
 *       or session cookies are created.</li>
 *   <li><b>Token-based Security:</b> Every request must include a valid Azure AD JWT token in the
 *       Authorization header, which cannot be automatically included by browsers in CSRF attacks.</li>
 *   <li><b>Same-Origin Policy:</b> Browsers' same-origin policy prevents malicious sites from reading
 *       the JWT token from localStorage/sessionStorage to include in forged requests.</li>
 * </ul>
 * 
 * <h3>Security Measures in Place:</h3>
 * <ul>
 *   <li>Azure AD JWT token validation via {@link AadAppRoleStatelessAuthenticationFilter}</li>
 *   <li>Method-level security with {@code @PreAuthorize} annotations on all endpoints</li>
 *   <li>Stateless session policy (SessionCreationPolicy.NEVER)</li>
 *   <li>Explicit whitelisting of public endpoints (health checks, API docs, etc.)</li>
 * </ul>
 * 
 * <p><b>Important:</b> If this service ever switches to cookie-based session authentication,
 * CSRF protection must be re-enabled immediately.
 * 
 * @see AadAppRoleStatelessAuthenticationFilter
 * @see org.opengroup.osdu.partition.api.PartitionApi
 */
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnProperty(value = "azure.istio.auth.enabled", havingValue = "false", matchIfMissing = false)
public class AADSecurityConfig {

    @Autowired
    private AadAppRoleStatelessAuthenticationFilter appRoleAuthFilter;

    /**
     * Public endpoints that do not require authentication.
     * These are health checks, API documentation, and service information endpoints.
     */
    public static final String[] AUTH_ALLOWLIST = {"/", "/index.html",
            "/actuator/*",
            "/api-docs.yaml",
            "/api-docs/swagger-config",
            "/api-docs/**",
            "/info",
            "/swagger",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/liveness_check"
    };

    /**
     * Configures the security filter chain for stateless JWT authentication.
     * 
     * <p>This configuration:
     * <ul>
     *   <li>Disables CSRF protection (not needed for stateless JWT APIs - see class-level documentation)</li>
     *   <li>Sets session policy to NEVER (no server-side sessions)</li>
     *   <li>Permits access to public endpoints without authentication</li>
     *   <li>Applies Azure AD JWT validation filter to all other requests</li>
     *   <li>Returns 401 UNAUTHORIZED for unauthenticated requests</li>
     * </ul>
     * 
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF disabled - stateless JWT API (see class documentation for full justification)
                .csrf(AbstractHttpConfigurer::disable)
                // Stateless session - no server-side sessions or cookies
                .sessionManagement((sess) -> sess.sessionCreationPolicy(SessionCreationPolicy.NEVER))
                // Public endpoints - health checks and API documentation
                .authorizeHttpRequests(request -> request.requestMatchers(AUTH_ALLOWLIST).permitAll())
                // Azure AD JWT token validation filter
                .addFilterBefore(appRoleAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Return 401 for unauthenticated requests
                .exceptionHandling((exceptionHandling) -> exceptionHandling.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .build();
    }

    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }

}
