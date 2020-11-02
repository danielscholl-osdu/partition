package org.opengroup.osdu.partition.provider.azure.cache;

import org.opengroup.osdu.core.common.cache.VmCache;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionServiceCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Qualifier("partitionListCache")
public class PartitionListCacheImpl extends VmCache<String, List<String>> implements IPartitionServiceCache<String, List<String>> {

    public PartitionListCacheImpl() {
        super(5 * 60, 1000);
    }
}
