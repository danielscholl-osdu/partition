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

package org.opengroup.osdu.partition.middleware;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.Request;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PartitionFilterTest {

    @Mock
    private DpsHeaders headers;
    @Mock
    private JaxRsDpsLog logger;
    @InjectMocks
    private PartitionFilter partitionFilter;

    @Test
    public void shouldSetCorrectResponseHeaders() throws IOException, ServletException {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("https://test.com"));
        FilterChain filterChain = mock(FilterChain.class);
        when(headers.getCorrelationId()).thenReturn("correlation-id-value");
        when(httpServletRequest.getMethod()).thenReturn("POST");

        partitionFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(httpServletResponse).addHeader("Access-Control-Allow-Origin", singletonList("*").toString());
        verify(httpServletResponse).addHeader("Access-Control-Allow-Headers", singletonList("origin, content-type, accept, authorization, data-partition-id, correlation-id, appkey").toString());
        verify(httpServletResponse).addHeader("Access-Control-Allow-Methods", singletonList("GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH").toString());
        verify(httpServletResponse).addHeader("Access-Control-Allow-Credentials", singletonList("true").toString());
        verify(httpServletResponse).addHeader("X-Frame-Options", singletonList("DENY").toString());
        verify(httpServletResponse).addHeader("X-XSS-Protection", singletonList("1; mode=block").toString());
        verify(httpServletResponse).addHeader("X-Content-Type-Options", singletonList("nosniff").toString());
        verify(httpServletResponse).addHeader("Cache-Control", "private, max-age=300");
        verify(httpServletResponse).addHeader("Content-Security-Policy", singletonList("default-src 'self'").toString());
        verify(httpServletResponse).addHeader("Strict-Transport-Security", singletonList("max-age=31536000; includeSubDomains").toString());
        verify(httpServletResponse).addHeader("Expires", singletonList("0").toString());
        verify(httpServletResponse).addHeader("correlation-id", "correlation-id-value");
        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
        verify(logger).request(Mockito.any(Request.class));
    }

    @Test
    public void redirectHttp() throws IOException, ServletException {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("http://test.com"));
        FilterChain filterChain = mock(FilterChain.class);
        when(httpServletRequest.getMethod()).thenReturn("POST");

        partitionFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(httpServletResponse).setStatus(307);
    }

    @Test
    public void optionsHasOkStatus() throws IOException, ServletException {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("https://test.com"));
        FilterChain filterChain = mock(FilterChain.class);
        when(httpServletRequest.getMethod()).thenReturn("OPTIONS");

        partitionFilter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        verify(httpServletResponse).setStatus(200);
    }
}
