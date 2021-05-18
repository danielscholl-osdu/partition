package org.opengroup.osdu.partition.provider.azure.service;

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionServiceCache;
import org.opengroup.osdu.partition.service.DefaultHealthCheckImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
public class HealthCheckServiceImpl extends DefaultHealthCheckImpl {

    @Autowired
    @Qualifier("partitionServiceCache")
    private IPartitionServiceCache<String, PartitionInfo> partitionServiceCache;

    @Value("${redis.custom.readiness.check.enabled}")
    private boolean redisCustomReadinessCheck;


    /**
     * Cache layer must be ready before the pod can serve the traffic
     */
    @Override
    public void performReadinessCheck() {
        if (redisCustomReadinessCheck) {
            partitionServiceCache.get("dummy-key");
        }
    }
}
