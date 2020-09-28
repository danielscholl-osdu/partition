package org.opengroup.osdu.partition.provider.azure.di;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class CloudTableConfiguration {

    @Value("${partition.cloud.table-name:PartitionInfo}")
    private String cloudTableName;
}
