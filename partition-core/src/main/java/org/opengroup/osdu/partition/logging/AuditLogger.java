package org.opengroup.osdu.partition.logging;

import com.google.common.base.Strings;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsService;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;
import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@RequiredArgsConstructor
public class AuditLogger {

  private final JaxRsDpsLog logger;

  private final IEntitlementsFactory factory;

  private final DpsHeaders headers;

  private AuditEvents events = null;

  private AuditEvents getAuditEvents() {
    if (this.events == null) {
      if (Strings.isNullOrEmpty(this.headers.getUserEmail())) {
        IEntitlementsService service = this.factory.create(headers);
        try {
          Groups groups = service.getGroups();
          this.events = new AuditEvents(groups.getMemberEmail());
        } catch (EntitlementsException e) {
          throw new AppException(HttpStatus.UNAUTHORIZED.value(), "Authentication Failure",
              e.getMessage(), e);
        }
      } else {
        this.events = new AuditEvents(this.headers.getUserEmail());
      }
    }
    return this.events;
  }

  public void createPartitionSuccess(List<String> resources) {
    writeLog(getAuditEvents().getCreatePartitionEvent(AuditStatus.SUCCESS, resources));
  }

  public void createPartitionFailure(List<String> resources) {
    writeLog(getAuditEvents().getCreatePartitionEvent(AuditStatus.FAILURE, resources));
  }

  public void readPartitionSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadPartitionEvent(AuditStatus.SUCCESS, resources));
  }

  public void readPartitionFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadPartitionEvent(AuditStatus.FAILURE, resources));
  }

  public void deletePartitionSuccess(List<String> resources) {
    writeLog(getAuditEvents().getDeletePartitionEvent(AuditStatus.SUCCESS, resources));
  }

  public void deletePartitionFailure(List<String> resources) {
    writeLog(getAuditEvents().getDeletePartitionEvent(AuditStatus.FAILURE, resources));
  }

  public void readServiceLivenessSuccess(List<String> resources) {
    writeLog(getAuditEvents().getReadServiceLivenessEvent(AuditStatus.SUCCESS, resources));
  }

  public void readServiceLivenessFailure(List<String> resources) {
    writeLog(getAuditEvents().getReadServiceLivenessEvent(AuditStatus.FAILURE, resources));
  }

  public void updatePartitionSecretSuccess(List<String> resources) {
    writeLog(getAuditEvents().getUpdatePartitionSecretEvent(AuditStatus.SUCCESS, resources));
  }

  public void updatePartitionSecretFailure(List<String> resources) {
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