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

package org.opengroup.osdu.partition.provider.azure.utils;

import com.microsoft.azure.spring.autoconfigure.aad.UserPrincipal;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWTClaimsSet;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultJws;
import lombok.Getter;
import net.minidev.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationServiceTest {

    @Mock
    private Authentication auth;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthorizationService authorizationService;

    @Before
    public void setup() {
        securityContext = Mockito.mock(SecurityContext.class);
        auth = Mockito.mock(Authentication.class);
    }

    private UserPrincipal createAADUserPrincipal(String claimName, String claimValue, String issuer) {
        final JSONArray claims = new JSONArray();
        final JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                //.subject("subject")
                .claim(claimName, claimValue)
                .issuer(issuer)
                .build();
        final JWSObject jwsObject = new JWSObject(new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                new Payload(jwtClaimsSet.toString()));
        return new UserPrincipal(jwsObject, jwtClaimsSet);
    }

    private DummyAuthToken createSAuthToken(final String email, final String appcode) {
        final Map<String, Object> map = new HashMap<>();
        map.put("email", email);
        map.put("appcode", appcode);
        map.put("iss", "sauth-preview.slb.com");
        Jws<Claims> jws = new DefaultJws<>(null, new DefaultClaims(map), null);
        return new DummyAuthToken(jws);
    }

    private void createSAuthTokenSetSecurityContext(final String email, final String appcode) {
        DummyAuthToken dummyAuthToken = createSAuthToken(email, appcode);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(dummyAuthToken);
    }

    private UserPrincipal createAADUserPrincipalSetSecurityContext(String claimName, String claimValue, String issuer) {
        UserPrincipal dummyAADPrincipal = createAADUserPrincipal(claimName, claimValue, issuer);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(dummyAADPrincipal);
        return dummyAADPrincipal;
    }

    @Test
    public void shouldReturnFalseWhenSAuthTokenIsSetInContext() {
        createSAuthTokenSetSecurityContext("email", null);
        assertFalse(authorizationService.isDomainAdminServiceAccount());
    }

    @Test
    public void shouldReturnTrueWhenAADTokenIsSetInContext() {
        createAADUserPrincipalSetSecurityContext(TestUtils.APPID, TestUtils.getAppId(), TestUtils.getAadIssuer());
        assertTrue(authorizationService.isDomainAdminServiceAccount());
    }

    @Getter
    public class DummyAuthToken {

        private final Jws<Claims> jws;

        public DummyAuthToken(Jws<Claims> jws) {
            this.jws = jws;
        }

        public <T> T getClaim(String claim, Class<T> type) {
            return jws.getBody().get(claim, type);
        }

        public String getIssuer() {
            return jws.getBody().getIssuer();
        }
    }
}
