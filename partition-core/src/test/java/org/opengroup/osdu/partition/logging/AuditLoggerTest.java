package org.opengroup.osdu.partition.logging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;

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
}