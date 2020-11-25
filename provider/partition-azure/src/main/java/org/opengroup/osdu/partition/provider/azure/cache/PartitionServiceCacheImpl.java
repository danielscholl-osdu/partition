package org.opengroup.osdu.partition.provider.azure.cache;

import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionServiceCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Qualifier("partitionServiceCache")
public class PartitionServiceCacheImpl implements IPartitionServiceCache<String, PartitionInfo> {

    @Resource(name="partitionServiceCache")
    private ICache<String, PartitionInfo> cache;

    @Override
    public void put(String s, PartitionInfo o) {
        this.cache.put(s, o);
    }

    @Override
    public PartitionInfo get(String s) {
        return this.cache.get(s);
    }

    @Override
    public void delete(String s) {
        this.cache.delete(s);
    }

    @Override
    public void clearAll() {
        this.cache.clearAll();
    }
}
