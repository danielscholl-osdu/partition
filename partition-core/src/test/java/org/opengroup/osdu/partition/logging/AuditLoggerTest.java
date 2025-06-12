/*
  Copyright 2002-2021 Google LLC
  Copyright 2002-2021 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.opengroup.osdu.partition.logging;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AuditLoggerTest {

    @Mock
    private JaxRsDpsLog log;

    @InjectMocks
    private AuditLogger sut;

    private List<String> resources;

    @Before
    public void setup() {
        resources = Collections.singletonList("resources");
    }

    @Test
    public void should_writeCreatePartitionSuccessEvent() {
        this.sut.createPartitionSuccess(this.resources);

        verify(this.log, times(1)).audit(any());
    }

    @Test
    public void should_writeCreatePartitionFailureEvent() {
        this.sut.createPartitionFailure(this.resources);

        verify(this.log, times(1)).audit(any());
    }

    @Test
    public void should_writeReadPartitionSuccessEvent() {
        this.sut.readPartitionSuccess(this.resources);

        verify(this.log, times(1)).audit(any());
    }

    @Test
    public void should_writeReadPartitionFailureEvent() {
        this.sut.readPartitionFailure(this.resources);

        verify(this.log, times(1)).audit(any());
    }

    @Test
    public void should_writeDeletePartitionSuccessEvent() {
        this.sut.deletePartitionSuccess(this.resources);

        verify(this.log, times(1)).audit(any());
    }

    @Test
    public void should_writeDeletePartitionFailureEvent() {
        this.sut.deletePartitionFailure(this.resources);

        verify(this.log, times(1)).audit(any());
    }

    @Test
    public void should_writeReadServiceLivenessSuccessEvent() {
        this.sut.readServiceLivenessSuccess(this.resources);

        verify(this.log, times(1)).audit(any());
    }

    @Test
    public void should_writeReadServiceLivenessFailureEvent() {
        this.sut.readServiceLivenessFailure(this.resources);

        verify(this.log, times(1)).audit(any());
    }

    @Test
    public void should_writeUpdatePartitionSecretSuccessEvent() {
        this.sut.updatePartitionSecretSuccess(this.resources);

        verify(this.log, times(1)).audit(any());
    }

    @Test
    public void should_writeUpdatePartitionSecretFailureEvent() {
        this.sut.updatePartitionSecretFailure(this.resources);

        verify(this.log, times(1)).audit(any());
    }

    @Test
    public void should_writeReadListPartitionSuccessEvent() {
        this.sut.readListPartitionSuccess(this.resources);

        verify(this.log, times(1)).audit(any());
    }

    @Test
    public void should_writeReadListPartitionFailureEvent() {
        this.sut.readListPartitionFailure(this.resources);

        verify(this.log, times(1)).audit(any());
    }

    @Test
    public void should_initializeAuditEvents_onlyOnce() {
        this.sut.readListPartitionFailure(new ArrayList<>());
        Object events1 = ReflectionTestUtils.getField(this.sut, "events");
        this.sut.readListPartitionFailure(this.resources);
        Object events2 = ReflectionTestUtils.getField(this.sut, "events");

        assertEquals(events1.hashCode(), events2.hashCode());
        verify(this.log, times(2)).audit(any());
    }
}
