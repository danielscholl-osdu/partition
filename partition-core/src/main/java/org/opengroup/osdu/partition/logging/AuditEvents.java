package org.opengroup.osdu.partition.logging;

import static java.lang.String.format;

import com.google.common.base.Strings;
import java.util.List;
import org.opengroup.osdu.core.common.logging.audit.AuditAction;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.logging.audit.AuditStatus;


public class AuditEvents {

  private static final String CREATE_PARTITION_ACTION_ID = "PT001";
  private static final String CREATE_PARTITION_MESSAGE = "Partition creation";

  private static final String READ_PARTITION_ACTION_ID = "PT002";
  private static final String READ_PARTITION_MESSAGE = "Partition read";

  private static final String DELETE_PARTITION_ACTION_ID = "PT003";
  private static final String DELETE_PARTITION_MESSAGE = "Partition removal";

  private static final String READ_SERVICE_LIVENESS_ACTION_ID = "PT004";
  private static final String READ_SERVICE_LIVENESS_MESSAGE = "Service run";

  private static final String UPDATE_PARTITION_ACTION_ID = "PT005";
  private static final String UPDATE_PARTITION_MESSAGE = "Partition updating";

  private static final String READ_LIST_PARTITION_ACTION_ID = "PT006";
  private static final String READ_LIST_PARTITION_MESSAGE = "Partition list read";


  private final String user;


  public AuditEvents(String user) {
    if (Strings.isNullOrEmpty(user)) {
      throw new IllegalArgumentException("User not provided for audit events.");
    }
    this.user = user;
  }

  public AuditPayload getCreatePartitionEvent(AuditStatus status, List<String> resources) {
    return AuditPayload.builder()
        .action(AuditAction.CREATE)
        .status(status)
        .user(this.user)
        .actionId(CREATE_PARTITION_ACTION_ID)
        .message(getStatusMessage(status, CREATE_PARTITION_MESSAGE))
        .resources(resources)
        .build();
  }

  public AuditPayload getReadPartitionEvent(AuditStatus status, List<String> resources) {
    return AuditPayload.builder()
        .action(AuditAction.READ)
        .status(status)
        .user(this.user)
        .actionId(READ_PARTITION_ACTION_ID)
        .message(getStatusMessage(status, READ_PARTITION_MESSAGE))
        .resources(resources)
        .build();
  }

  public AuditPayload getDeletePartitionEvent(AuditStatus status, List<String> resources) {
    return AuditPayload.builder()
        .action(AuditAction.DELETE)
        .status(status)
        .user(this.user)
        .actionId(DELETE_PARTITION_ACTION_ID)
        .message(getStatusMessage(status, DELETE_PARTITION_MESSAGE))
        .resources(resources)
        .build();
  }

  public AuditPayload getReadServiceLivenessEvent(AuditStatus status, List<String> resources) {
    return AuditPayload.builder()
        .action(AuditAction.READ)
        .status(status)
        .user(this.user)
        .actionId(READ_SERVICE_LIVENESS_ACTION_ID)
        .message(getStatusMessage(status, READ_SERVICE_LIVENESS_MESSAGE))
        .resources(resources)
        .build();
  }

  public AuditPayload getUpdatePartitionSecretEvent(AuditStatus status, List<String> resources) {
    return AuditPayload.builder()
        .action(AuditAction.UPDATE)
        .status(status)
        .user(this.user)
        .actionId(UPDATE_PARTITION_ACTION_ID)
        .message(getStatusMessage(status, UPDATE_PARTITION_MESSAGE))
        .resources(resources)
        .build();
  }

  public AuditPayload getListPartitionEvent(AuditStatus status, List<String> resources) {
    return AuditPayload.builder()
        .action(AuditAction.READ)
        .status(status)
        .user(this.user)
        .actionId(READ_LIST_PARTITION_ACTION_ID)
        .message(getStatusMessage(status, READ_LIST_PARTITION_MESSAGE))
        .resources(resources)
        .build();
  }

  private String getStatusMessage(AuditStatus status, String message) {
    return format("%s - %s", message, status.name().toLowerCase());
  }
}