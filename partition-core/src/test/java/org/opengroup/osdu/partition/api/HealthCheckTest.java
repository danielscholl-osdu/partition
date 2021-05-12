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

package org.opengroup.osdu.partition.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.partition.logging.AuditLogger;
import org.opengroup.osdu.partition.provider.interfaces.IHealthCheckService;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckTest {

    @Mock
    private AuditLogger auditLogger;
    @Mock
    private IHealthCheckService healthCheckService;

    @InjectMocks
    private HealthCheck sut;

    @Test
    public void should_returnHttp200_when_checkLiveness() {
        assertEquals(HttpStatus.OK, this.sut.livenessCheck().getStatusCode());
    }

    @Test(expected = Exception.class)
    public void should_throwException_when_customizedLivenessCheckFail() {
        doThrow(new Exception()).when(healthCheckService).performLivenessCheck();
        this.sut.livenessCheck();
    }

    @Test
    public void should_returnHttp200_when_checkReadiness() {
        assertEquals(HttpStatus.OK, this.sut.readinessCheck().getStatusCode());
        verify(healthCheckService).performReadinessCheck();
    }

    @Test(expected = Exception.class)
    public void should_throwException_when_customizedReadinessCheckFail() {
        doThrow(new Exception()).when(healthCheckService).performReadinessCheck();
        this.sut.readinessCheck();
    }
}