package org.opengroup.osdu.partition.service;

import org.opengroup.osdu.partition.provider.interfaces.IHealthCheckService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(value = IHealthCheckService.class)
public class DefaultHealthCheckImpl implements IHealthCheckService {

    @Override
    public void performLivenessCheck() {

    }

    @Override
    public void performReadinessCheck() {

    }
}
