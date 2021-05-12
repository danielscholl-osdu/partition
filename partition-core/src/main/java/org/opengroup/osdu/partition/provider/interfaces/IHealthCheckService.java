package org.opengroup.osdu.partition.provider.interfaces;

public interface IHealthCheckService {
    void performLivenessCheck();
    void performReadinessCheck();
}
