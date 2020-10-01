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

import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Map;

@RestController
@RequestScope
@RequestMapping(path = "/partitions", produces = "application/json")
public class PartitionApi {

    @Autowired
    @Qualifier("cachedPartitionServiceImpl")
    private IPartitionService partitionService;

    @PostMapping("/{partitionId}")
    @PreAuthorize("@authorizationFilter.hasPermissions()")
    public ResponseEntity create(@PathVariable("partitionId") String partitionId, @RequestBody @Valid PartitionInfo partitionInfo) {
        this.partitionService.createPartition(partitionId, partitionInfo);
        URI partitionLocation = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand().toUri();
        return ResponseEntity.created(partitionLocation).build();
    }

    @GetMapping("/{partitionId}")
    @PreAuthorize("@authorizationFilter.hasPermissions()")
    public ResponseEntity<Map<String, Object>> get(@PathVariable("partitionId") String partitionId) {
        PartitionInfo partitionInfo = this.partitionService.getPartition(partitionId);
        return ResponseEntity.ok(partitionInfo.getProperties());
    }

    @DeleteMapping("/{partitionId}")
    @PreAuthorize("@authorizationFilter.hasPermissions()")
    public ResponseEntity delete(@PathVariable("partitionId") String partitionId) {
        this.partitionService.deletePartition(partitionId);
        return ResponseEntity.noContent().build();
    }
}
