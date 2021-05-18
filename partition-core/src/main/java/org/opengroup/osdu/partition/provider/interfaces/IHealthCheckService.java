package org.opengroup.osdu.partition.provider.interfaces;

public interface IHealthCheckService {

    default void performLivenessCheck() {

    }

    default void performReadinessCheck() {

    }
}
