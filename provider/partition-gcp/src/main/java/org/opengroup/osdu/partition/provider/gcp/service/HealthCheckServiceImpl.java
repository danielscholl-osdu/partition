package org.opengroup.osdu.partition.provider.gcp.service;

import org.opengroup.osdu.partition.provider.interfaces.IHealthCheckService;
import org.springframework.stereotype.Service;

@Service
public class HealthCheckServiceImpl implements IHealthCheckService {

    @Override
    public void performReadinessCheck() {

    }
}
