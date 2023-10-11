// Copyright © 2020 Amazon Web Services
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

package org.opengroup.osdu.partition.provider.aws.security;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.aws.entitlements.Authorizer;
import org.opengroup.osdu.core.aws.entitlements.RequestKeys;
import org.opengroup.osdu.core.aws.ssm.K8sLocalParameterProvider;
import org.opengroup.osdu.core.aws.ssm.K8sParameterNotFoundException;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationServiceTest {

    @Mock
    private DpsHeaders headers;

    @Mock
    Authorizer authorizer;

    private final String spuEmail = "spu@email.com";

    @InjectMocks
    private AuthorizationService authorizationService;

    private final Map<String, String> validHeaders = new HashMap<>();
    private final Map<String, String> validLowerCaseHeaders = new HashMap<>();
    private final Map<String, String> noTokenHeaders = new HashMap<>();

    @Before
    public void setupGlobal() {
        ReflectionTestUtils.setField(authorizationService, "headers", headers);

        validHeaders.put(RequestKeys.AUTHORIZATION_HEADER_KEY, spuEmail);
        validLowerCaseHeaders.put(RequestKeys.AUTHORIZATION_HEADER_KEY.toLowerCase(), spuEmail);
        authorizationService.authorizer = authorizer;
        authorizationService.spuEmail = spuEmail;
    }

    @Test
    public void should_return_when_initCalled() throws K8sParameterNotFoundException {

        try (MockedConstruction<Authorizer> authorizer = Mockito.mockConstruction(Authorizer.class)) {
            try (MockedConstruction<K8sLocalParameterProvider> k8sParameterProvider = Mockito.mockConstruction(K8sLocalParameterProvider.class,
                   (mock, context) -> { when(mock.getParameterAsString(anyString())).thenReturn(spuEmail); })) {
                authorizationService.init();

                assertEquals(spuEmail, authorizationService.spuEmail);
            }
        }
    }

    @Test
    public void should_returnTrue_when_isDomainAdminServiceAccountCalledWithRightHeaders() {
        when(headers.getHeaders()).thenReturn(validHeaders);
        when(headers.getUserId()).thenReturn(spuEmail);
        System.out.println("should_returnTrue_when_isDomainAdminServiceAccountCalledWithRightHeaders: " + headers.getUserId() + ", " + authorizationService.spuEmail);

        try {
            assertTrue(authorizationService.isDomainAdminServiceAccount());
        } catch (AppException exception) {
            System.out.println("should_returnTrue_when_isDomainAdminServiceAccountCalledWithRightHeaders Error: " + exception.getError().getDebuggingInfo());
            System.out.flush();
            throw exception;
        }
    }

    @Test
    public void should_returnTrue_when_isDomainAdminServiceAccountCalledLowerCaseHeaders() {
        when(headers.getHeaders()).thenReturn(validLowerCaseHeaders);
        when(headers.getUserId()).thenReturn(spuEmail);

        System.out.println("should_returnTrue_when_isDomainAdminServiceAccountCalledLowerCaseHeaders: " + headers.getUserId() + ", " + authorizationService.spuEmail);

        try {
            assertTrue(authorizationService.isDomainAdminServiceAccount());
        } catch (AppException exception) {
            System.out.println("should_returnTrue_when_isDomainAdminServiceAccountCalledLowerCaseHeaders Error: " + exception.getError().getDebuggingInfo());
            System.out.flush();
            throw exception;
        }
    }

    @Test
    public void should_ThrowAppException_when_isDomainAdminServiceAccountCalledWithInvalidHeaders() {
        when(headers.getHeaders()).thenReturn(validHeaders);
        String nonSpuEmail = "not-the-spu@email.com";
        when(headers.getUserId()).thenReturn(nonSpuEmail);

        try {
            authorizationService.isDomainAdminServiceAccount();
            fail("Should have failed at previous step.");
        } catch (AppException exception) {
            assertEquals(401, exception.getError().getCode());
            assertTrue(exception.getError().getReason().equalsIgnoreCase("Unauthorized"));
            assertTrue(exception.getError().getMessage().equalsIgnoreCase("The user is not authorized to perform this action"));
        }
    }

    @Test
    public void should_ThrowAppException_when_isDomainAdminServiceAccountCalledWithoutAuthHeaders() {
        when(headers.getHeaders()).thenReturn(noTokenHeaders);

        try {
            authorizationService.isDomainAdminServiceAccount();
            fail("Should have failed at previous step.");
        } catch (AppException exception) {
            assertEquals(401, exception.getError().getCode());
            assertTrue(exception.getError().getReason().equalsIgnoreCase("Unauthorized"));
            assertTrue(exception.getError().getMessage().equalsIgnoreCase("The user is not authorized to perform this action"));
        }
    }

    @Test
    public void should_ThrowAppException_when_isDomainAdminServiceAccountCalledWithUnauthUser() {
        when(headers.getHeaders()).thenReturn(validHeaders);
        when(headers.getUserId()).thenReturn(null);

        try {
            authorizationService.isDomainAdminServiceAccount();
            fail("Should have failed at previous step.");
        } catch (AppException exception) {
            assertEquals(401, exception.getError().getCode());
            assertTrue(exception.getError().getReason().equalsIgnoreCase("Unauthorized"));
            assertTrue(exception.getError().getMessage().equalsIgnoreCase("The user is not authorized to perform this action"));
        }
    }

    @Test
    public void should_ThrowAppException_when_isDomainAdminServiceAccountHasInternalError() {
        doThrow(RuntimeException.class).when(headers).getHeaders();

        try {
            authorizationService.isDomainAdminServiceAccount();
            fail("Should have failed at previous step.");
        } catch (AppException exception) {
            assertEquals(500, exception.getError().getCode());
            assertTrue(exception.getError().getReason().equalsIgnoreCase("Authentication Failure"));
        }
    }

}
