package org.opengroup.osdu.service;

import static org.jboss.resteasy.reactive.RestResponse.Status.INTERNAL_SERVER_ERROR;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.opengroup.osdu.model.exception.AppException;

@ApplicationScoped
@Startup
@Getter
public class PartitionConfigProvider {
  private static final Logger log = Logger.getLogger(PartitionConfigProvider.class);

  @ConfigProperty(name = "partitionConfigsPath")
  private String partitionConfigsPath;

  @ConfigProperty(name = "groupId")
  private String groupId;

  @ConfigProperty(name = "artifactId")
  private String artifactId;

  @ConfigProperty(name = "version")
  private String version;

  @ConfigProperty(name = "buildTime")
  private String buildTime;

  @ConfigProperty(name = "gitPropertiesPath", defaultValue = "/git.properties")
  private String gitPropertiesPath;

  protected void setPartitionConfigsPath(String partitionConfigsPath) {
    this.partitionConfigsPath = partitionConfigsPath;
  }

  @PostConstruct
  protected void init() {
    if (partitionConfigsPath == null || partitionConfigsPath.trim().isEmpty()) {
      throw new AppException(
          INTERNAL_SERVER_ERROR.getStatusCode(),
          "Environment variable is not set",
          "The environment variable PARTITION_CONFIGS_PATH is required but not set");
    }
    log.infof("Partition Configs Path: %s", partitionConfigsPath);
    if (!Files.exists(Paths.get(partitionConfigsPath))) {
      log.errorf("Partition Configs Path: %s does not exist", partitionConfigsPath);
      throw new AppException(
          INTERNAL_SERVER_ERROR.getStatusCode(),
          "Partition Configs Path does not exist",
          "Partition Configs Path: " + partitionConfigsPath + " does not exist");
    }
  }
}
