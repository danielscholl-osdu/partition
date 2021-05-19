package org.opengroup.osdu.partition.provider.azure.service;

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionServiceCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
class CacheServiceHealthIndicatorImpl implements HealthIndicator {

    @Autowired
    @Qualifier("partitionServiceCache")
    private IPartitionServiceCache<String, PartitionInfo> partitionServiceCache;

    @Value("${redis.custom.readiness.check.enabled}")
    private boolean redisCustomReadinessCheck;

    @Override
    public Health health() {
        if (redisCustomReadinessCheck) {
            try {
                partitionServiceCache.get("dummy-key");
            } catch (Exception ex) {
                return Health.down().withDetail("Cache service", ex).build();
            }
        }
        return Health.up().build();
    }
}
