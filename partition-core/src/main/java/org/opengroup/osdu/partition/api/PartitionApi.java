// Copyright 2017-2020, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.partition.api;

import java.util.Collections;
import org.opengroup.osdu.partition.logging.AuditLogger;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestScope
@RequestMapping(path = "/partitions", produces = "application/json")
public class PartitionApi {

    @Autowired
    @Qualifier("cachedPartitionServiceImpl")
    private IPartitionService partitionService;

    @Autowired
    private AuditLogger auditLogger;

    @PostMapping("/{partitionId}")
    @PreAuthorize("@authorizationFilter.hasPermissions()")
    public ResponseEntity create(@PathVariable("partitionId") String partitionId, @RequestBody @Valid PartitionInfo partitionInfo) {
        this.partitionService.createPartition(partitionId, partitionInfo);
        URI partitionLocation = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand().toUri();
        this.auditLogger.createdPartitionSuccess(Collections.singletonList(partitionId));
        return ResponseEntity.created(partitionLocation).build();
    }

    @PatchMapping("/{partitionId}")
    @PreAuthorize("@authorizationFilter.hasPermissions()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patch(@PathVariable("partitionId") String partitionId, @RequestBody @Valid PartitionInfo partitionInfo) {
        this.partitionService.updatePartition(partitionId, partitionInfo);
        this.auditLogger.updatedPartitionSecretSuccess(Collections.singletonList(partitionId));
    }

    @GetMapping("/{partitionId}")
    @PreAuthorize("@authorizationFilter.hasPermissions()")
    public ResponseEntity<Map<String, Property>> get(@PathVariable("partitionId") String partitionId) {
        PartitionInfo partitionInfo = this.partitionService.getPartition(partitionId);
        this.auditLogger.readPartitionSuccess(Collections.singletonList(partitionId));
        return ResponseEntity.ok(partitionInfo.getProperties());
    }

    @DeleteMapping("/{partitionId}")
    @PreAuthorize("@authorizationFilter.hasPermissions()")
    public ResponseEntity delete(@PathVariable("partitionId") String partitionId) {
        this.partitionService.deletePartition(partitionId);
        this.auditLogger.deletedPartitionSuccess(Collections.singletonList(partitionId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("@authorizationFilter.hasPermissions()")
    public List<String> list() {
        List<String> partitions = this.partitionService.getAllPartitions();
        this.auditLogger.readListPartitionSuccess(
            Collections.singletonList(String.format("Partition list size = %s", partitions.size())));
        return partitions;
    }
}
