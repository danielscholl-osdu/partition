package org.opengroup.osdu.partition.provider.azure.cache;

import org.opengroup.osdu.core.common.cache.ICache;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionServiceCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Qualifier("partitionListCache")
public class PartitionListCacheImpl implements IPartitionServiceCache<String, List<String>> {

    @Resource(name="partitionListCache")
    private ICache<String, List<String>> cache;

    @Override
    public void put(String s, List<String> o) {
        this.cache.put(s, o);
    }

    @Override
    public List<String> get(String s) {
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
