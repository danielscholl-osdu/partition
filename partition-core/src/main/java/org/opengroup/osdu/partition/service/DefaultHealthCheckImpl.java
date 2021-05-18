package org.opengroup.osdu.partition.service;

import org.opengroup.osdu.partition.provider.interfaces.IHealthCheckService;
import org.springframework.stereotype.Service;

@Service
public class DefaultHealthCheckImpl implements IHealthCheckService {

    @Override
    public void performLivenessCheck() {

    }

    @Override
    public void performReadinessCheck() {

    }
}
