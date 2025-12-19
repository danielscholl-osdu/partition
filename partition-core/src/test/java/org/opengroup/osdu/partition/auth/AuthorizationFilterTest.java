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

package org.opengroup.osdu.partition.auth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.partition.provider.interfaces.IAuthorizationService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizationFilterTest {

    @Mock
    private IAuthorizationService authorizationService;
    @InjectMocks
    private AuthorizationFilter sut;


    @Test
    public void should_authenticateRequest_when_resourceIsRolesAllowedAnnotated() {
        when(this.authorizationService.isDomainAdminServiceAccount()).thenReturn(true);

        assertTrue(this.sut.hasPermissions());
    }

    @Test
    public void should_throwAppError_when_noAuthzProvided() {
        when(this.authorizationService.isDomainAdminServiceAccount()).thenReturn(false);

        assertFalse(this.sut.hasPermissions());
    }
}