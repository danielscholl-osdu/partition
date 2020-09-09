package org.opengroup.osdu.partition.provider.azure.service;

import org.opengroup.osdu.core.common.cache.RedisCache;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionServiceCache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PartitionServiceCacheImpl extends RedisCache<String, PartitionInfo> implements IPartitionServiceCache {
    public PartitionServiceCacheImpl(@Value("${REDIS_HOST}") final String host
            , @Value("${REDIS_PORT}") final int port) {
        super(host, port, 60*60, String.class, PartitionInfo.class);
    }
}
