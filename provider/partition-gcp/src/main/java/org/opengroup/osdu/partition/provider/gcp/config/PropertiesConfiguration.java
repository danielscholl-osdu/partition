package org.opengroup.osdu.partition.provider.gcp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
@Getter
@Setter
public class PropertiesConfiguration {

  private String authorizeApi;

  private String authorizeApiKey;

  private int cacheExpiration;

  private int cacheMaxSize;
}
