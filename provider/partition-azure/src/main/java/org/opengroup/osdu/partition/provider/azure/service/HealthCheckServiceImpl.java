package org.opengroup.osdu.partition.provider.azure.service;

import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.interfaces.IHealthCheckService;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionServiceCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthCheckServiceImpl implements IHealthCheckService {

    @Autowired
    @Qualifier("partitionServiceCache")
    private IPartitionServiceCache<String, PartitionInfo> partitionServiceCache;

    @Override
    public void performLivenessCheck() {

    }

    /**
     * Cache layer must be ready before the pod can serve the traffic
     */
    @Override
    public void performReadinessCheck() {
        partitionServiceCache.get("dummy-key");
    }
}
