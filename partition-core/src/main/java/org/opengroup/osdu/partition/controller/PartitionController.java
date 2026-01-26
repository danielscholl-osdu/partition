package org.opengroup.osdu.partition.controller;

import static org.opengroup.osdu.partition.service.PartitionServiceRole.REQUIRED_GROUPS;

import org.opengroup.osdu.partition.api.PartitionApi;
import org.opengroup.osdu.partition.logging.AuditLogger;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class PartitionController implements PartitionApi {
    @Autowired
    @Qualifier("partitionServiceImpl")
    private IPartitionService partitionService;

    @Autowired
    private AuditLogger auditLogger;

    @Override
    public ResponseEntity create(String partitionId, PartitionInfo partitionInfo) {
        this.partitionService.createPartition(partitionId, partitionInfo);
        URI partitionLocation = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand().toUri();
        this.auditLogger.createPartitionSuccess(Collections.singletonList(partitionId), REQUIRED_GROUPS);
        return ResponseEntity.created(partitionLocation).build();
    }

    @Override
    public void patch(String partitionId, PartitionInfo partitionInfo) {
        this.partitionService.updatePartition(partitionId, partitionInfo);
        this.auditLogger.updatePartitionSecretSuccess(Collections.singletonList(partitionId), REQUIRED_GROUPS);
    }

    @Override
    public ResponseEntity<Map<String, Property>> get(String partitionId) {
        PartitionInfo partitionInfo = this.partitionService.getPartition(partitionId);
        this.auditLogger.readPartitionSuccess(Collections.singletonList(partitionId), REQUIRED_GROUPS);
        return ResponseEntity.ok(partitionInfo.getProperties());
    }

    @Override
    public ResponseEntity delete(String partitionId) {
        this.partitionService.deletePartition(partitionId);
        this.auditLogger.deletePartitionSuccess(Collections.singletonList(partitionId), REQUIRED_GROUPS);
        return ResponseEntity.noContent().build();
    }

    @Override
    public List<String> list() {
        List<String> partitions = this.partitionService.getAllPartitions();
        this.auditLogger.readListPartitionSuccess(
                Collections.singletonList(String.format("Partition list size = %s", partitions.size())), REQUIRED_GROUPS);
        return partitions;
    }
}
