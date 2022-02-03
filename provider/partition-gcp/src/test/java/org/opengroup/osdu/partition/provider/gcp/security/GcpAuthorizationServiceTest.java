/*
 * Copyright 2021 Google LLC
 * Copyright 2021 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.partition.provider.gcp.security;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.partition.provider.gcp.config.PropertiesConfiguration;

@RunWith(Theories.class)
public class GcpAuthorizationServiceTest {

    private final String token = "abc";

    private final String serviceAccountTail = "project-id.iam.gserviceaccount.com";

    private final List<String> partitionAdminAccounts = ImmutableList.of("osdu-gcp-sa", "service.account@project-id.iam.gserviceaccount.com");

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @DataPoints("VALID_ACCOUNTS")
    public static List<String> validTestSet() {
        return ImmutableList.of(
            "osdu-gcp-sa-first@project-id.iam.gserviceaccount.com",
            "osdu-gcp-sa-second@project-id.iam.gserviceaccount.com",
            "osdu-gcp-sa-third@project-id.iam.gserviceaccount.com",
            "osdu-gcp-sa-fourth@project-id.iam.gserviceaccount.com");
    }

    @DataPoints("NOT_VALID_ACCOUNTS")
    public static List<String> notValidTestSet() {
        return ImmutableList.of(
            "osdu-gcp-sa-first@google.com",
            "osdu-gcp-sa-second@project-id.iam.gserviceaccount.com.not.valid",
            "user-osdu-gcp-sa-third@project-id.iam.gserviceaccount.com");
    }

    @Mock
    private PropertiesConfiguration configuration;

    @Mock
    private GoogleIdTokenVerifier verifier;

    @Mock
    private GoogleIdToken googleIdToken;

    @Mock
    private DpsHeaders headers;

    private Payload payload = new Payload();

    @InjectMocks
    private GcpAuthorizationService gcpAuthorizationService;

    @Before
    public void setUp() throws GeneralSecurityException, IOException {
        MockitoAnnotations.initMocks(this);
        when(configuration.getPartitionAdminAccounts()).thenReturn(partitionAdminAccounts);
        when(configuration.getServiceAccountTail()).thenReturn(serviceAccountTail);
        when(headers.getAuthorization()).thenReturn(token);
        when(verifier.verify(token)).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);

    }

    @Test
    public void testProvidedInConfigAdminAccountShouldReturnTrue() {
        payload.setEmail("service.account@project-id.iam.gserviceaccount.com");
        assertTrue(gcpAuthorizationService.isDomainAdminServiceAccount());
    }

    @Test(expected = AppException.class)
    public void testNotProvidedInConfigAdminAccountShouldThrowException() {
        payload.setEmail("user@google.com");
        gcpAuthorizationService.isDomainAdminServiceAccount();
    }

    @Theory
    public void testProvidedInConfigPatternShouldReturnTrue(@FromDataPoints("VALID_ACCOUNTS") String account) {
        payload.setEmail(account);
        assertTrue(gcpAuthorizationService.isDomainAdminServiceAccount());
    }

    @Theory
    public void testNotProvidedInConfigPatternShouldReturnTrue(@FromDataPoints("NOT_VALID_ACCOUNTS") String account) {
        exceptionRule.expect(AppException.class);
        payload.setEmail(account);
        gcpAuthorizationService.isDomainAdminServiceAccount();
    }
}