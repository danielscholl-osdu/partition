package org.opengroup.osdu.partition.provider.azure.security;

import com.azure.spring.autoconfigure.aad.UserPrincipal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AzureIstioSecurityFilterTest {
    private static final String X_ISTIO_CLAIMS_PAYLOAD = "x-payload";
    private static final String ISTIO_PAYLOAD = "{\"aud\":\"aud1\",\"iss\":\"https://iss1\",\"ver\":\"1.0\"}";

    @InjectMocks
    private AzureIstioSecurityFilter azureIstioSecurityFilter;

    @Test
    public void should_setCorrectAuthentication_when_istioPayloadExists() throws IOException, ServletException {
        ArgumentCaptor<PreAuthenticatedAuthenticationToken> authCaptor = ArgumentCaptor.forClass(PreAuthenticatedAuthenticationToken.class);
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        FilterChain filterChain = Mockito.mock(FilterChain.class);
        Enumeration<String> headerNames = Collections.enumeration(Arrays.asList("Content-Type", "header1"));
        SecurityContextHolder.setContext(securityContext);
        when(httpServletRequest.getHeaderNames()).thenReturn(headerNames);
        when(httpServletRequest.getHeader(X_ISTIO_CLAIMS_PAYLOAD)).thenReturn(new String(Base64.getEncoder().encode(ISTIO_PAYLOAD.getBytes())));

        azureIstioSecurityFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(securityContext).setAuthentication(authCaptor.capture());
        PreAuthenticatedAuthenticationToken auth = authCaptor.getValue();
        assert_UserPrincipal((UserPrincipal) auth.getPrincipal());
        assertNull(auth.getCredentials());
        assertEquals(auth.getAuthorities(), Collections.EMPTY_LIST);
        Mockito.verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
    }

    private void assert_UserPrincipal(UserPrincipal principal) {
        assertEquals(3, principal.getClaims().size());
        assertEquals(Arrays.asList("aud1"), principal.getClaims().get("aud"));
        assertEquals("https://iss1", principal.getClaims().get("iss"));
        assertEquals("1.0", principal.getClaims().get("ver"));
    }
}
