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

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.util.IpAddressUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@RequiredArgsConstructor
public class AuditLogger {

  private final JaxRsDpsLog logger;
  private final DpsHeaders headers;
  private final HttpServletRequest httpServletRequest;

  private AuditEvents events = null;

  private AuditEvents getAuditEvents() {
    if (this.events == null) {
      String userIpAddress = IpAddressUtil.getClientIpAddress(httpServletRequest);
      String userAgent = httpServletRequest.getHeader("user-agent");
      this.events = new AuditEvents("partitionAccountUser",
                                    userIpAddress, userAgent,
                                    headers.getUserAuthorizedGroupName());
    }
    return this.events;
  }

  public void createPartitionSuccess(List<String> resources, List<String> requiredGroupsForAction) {
    writeLog(getAuditEvents().getCreatePartitionEvent(AuditStatus.SUCCESS, resources, requiredGroupsForAction));
  }

  public void createPartitionFailure(List<String> resources, List<String> requiredGroupsForAction) {
    writeLog(getAuditEvents().getCreatePartitionEvent(AuditStatus.FAILURE, resources, requiredGroupsForAction));
  }

  public void readPartitionSuccess(List<String> resources, List<String> requiredGroupsForAction) {
    writeLog(getAuditEvents().getReadPartitionEvent(AuditStatus.SUCCESS, resources, requiredGroupsForAction));
  }

  public void readPartitionFailure(List<String> resources, List<String> requiredGroupsForAction) {
    writeLog(getAuditEvents().getReadPartitionEvent(AuditStatus.FAILURE, resources, requiredGroupsForAction));
  }

  public void deletePartitionSuccess(List<String> resources, List<String> requiredGroupsForAction) {
    writeLog(getAuditEvents().getDeletePartitionEvent(AuditStatus.SUCCESS, resources, requiredGroupsForAction));
  }

  public void deletePartitionFailure(List<String> resources, List<String> requiredGroupsForAction) {
    writeLog(getAuditEvents().getDeletePartitionEvent(AuditStatus.FAILURE, resources, requiredGroupsForAction));
  }

  public void readServiceLivenessSuccess(List<String> resources, List<String> requiredGroupsForAction) {
    writeLog(getAuditEvents().getReadServiceLivenessEvent(AuditStatus.SUCCESS, resources, requiredGroupsForAction));
  }

  public void readServiceLivenessFailure(List<String> resources, List<String> requiredGroupsForAction) {
    writeLog(getAuditEvents().getReadServiceLivenessEvent(AuditStatus.FAILURE, resources, requiredGroupsForAction));
  }

  public void updatePartitionSecretSuccess(List<String> resources, List<String> requiredGroupsForAction) {
    writeLog(getAuditEvents().getUpdatePartitionSecretEvent(AuditStatus.SUCCESS, resources, requiredGroupsForAction));
  }

  public void updatePartitionSecretFailure(List<String> resources, List<String> requiredGroupsForAction) {
    writeLog(getAuditEvents().getUpdatePartitionSecretEvent(AuditStatus.FAILURE, resources, requiredGroupsForAction));
  }

  public void readListPartitionSuccess(List<String> resources, List<String> requiredGroupsForAction) {
    writeLog(getAuditEvents().getListPartitionEvent(AuditStatus.SUCCESS, resources, requiredGroupsForAction));
  }

  public void readListPartitionFailure(List<String> resources, List<String> requiredGroupsForAction) {
    writeLog(getAuditEvents().getListPartitionEvent(AuditStatus.FAILURE, resources, requiredGroupsForAction));
  }

  private void writeLog(AuditPayload log) {
    this.logger.audit(log);
  }
}
