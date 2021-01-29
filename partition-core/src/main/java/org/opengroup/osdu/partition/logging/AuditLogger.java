package org.opengroup.osdu.partition.logging;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@RequiredArgsConstructor
public class AuditLogger {

  private final JaxRsDpsLog logger;

  private final DpsHeaders headers;

  private AuditEvents events = null;

  private AuditEvents getAuditEvents() {
    if (this.events == null) {
      this.events = new AuditEvents(this.headers.getUserEmail());
    }
    return this.events;
  }

  public void createdPartitionSuccess(List<String> resources) {
    writeLog(getAuditEvents().getCreatePartitionEvent(AuditStatus.SUCCESS, resources));
  }

  public void createdPartitionFailure(List<String> resources) {
    writeLog(getAuditEvents().getCreatePartitionEvent(AuditStatus.FAILURE, resources));
  }

  public void readPartitionSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadPartitionEvent(AuditStatus.SUCCESS, resources));
  }

  public void readPartitionFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadPartitionEvent(AuditStatus.FAILURE, resources));
  }

  public void deletedPartitionSuccess(List<String> resources) {
    writeLog(getAuditEvents().getDeletePartitionEvent(AuditStatus.SUCCESS, resources));
  }

  public void deletedPartitionFailure(List<String> resources) {
    writeLog(getAuditEvents().getDeletePartitionEvent(AuditStatus.FAILURE, resources));
  }

  public void readServiceLivenessSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadServiceLivenessEvent(AuditStatus.SUCCESS, resources));
  }

  public void readServiceLivenessFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadServiceLivenessEvent(AuditStatus.FAILURE, resources));
  }

  public void updatedPartitionSecretSuccess(List<String> resources) {
    writeLog(getAuditEvents().getUpdatePartitionSecretEvent(AuditStatus.SUCCESS, resources));
  }

  public void updatedPartitionSecretFailure(List<String> resources) {
    writeLog(getAuditEvents().getUpdatePartitionSecretEvent(AuditStatus.FAILURE, resources));
  }

  public void readListPartitionSuccess(List<String> resources) {
    writeLog(getAuditEvents().getListPartitionEvent(AuditStatus.SUCCESS, resources));
  }

  public void readListPartitionFailure(List<String> resources) {
    writeLog(getAuditEvents().getListPartitionEvent(AuditStatus.FAILURE, resources));
  }

  private void writeLog(AuditPayload log) {
    this.logger.audit(log);
  }
}