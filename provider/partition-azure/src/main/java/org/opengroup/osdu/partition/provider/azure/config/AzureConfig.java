package org.opengroup.osdu.partition.provider.azure.config;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfig {

    @Bean
    public TelemetryClient telemetryClient() {
        return new TelemetryClient();
    }

}
