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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.opengroup.osdu.core.common.model.http.AppError;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestScope
@RequestMapping(path = "/partitions", produces = "application/json")
@Tag(name = "partition-api", description = "Partition API")
public class PartitionApi {

    @Autowired
    @Qualifier("partitionServiceImpl")
    private IPartitionService partitionService;

    @Autowired
    private AuditLogger auditLogger;

    @Operation(summary = "${partitionApi.create.summary}", description = "${partitionApi.create.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "partition-api" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @PostMapping("/{partitionId}")
    @PreAuthorize("@authorizationFilter.hasPermissions()")
    public ResponseEntity create(@Parameter(description = "Partition Id") @PathVariable("partitionId") String partitionId,
                                 @RequestBody @Valid PartitionInfo partitionInfo) {
        this.partitionService.createPartition(partitionId, partitionInfo);
        URI partitionLocation = ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand().toUri();
        this.auditLogger.createPartitionSuccess(Collections.singletonList(partitionId));
        return ResponseEntity.created(partitionLocation).build();
    }

    @Operation(summary = "${partitionApi.patch.summary}", description = "${partitionApi.patch.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "partition-api" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @PatchMapping("/{partitionId}")
    @PreAuthorize("@authorizationFilter.hasPermissions()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void patch(@Parameter(description = "Partition Id") @PathVariable("partitionId") String partitionId,
                      @RequestBody @Valid PartitionInfo partitionInfo) {
        this.partitionService.updatePartition(partitionId, partitionInfo);
        this.auditLogger.updatePartitionSecretSuccess(Collections.singletonList(partitionId));
    }

    @Operation(summary = "${partitionApi.get.summary}", description = "${partitionApi.get.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "partition-api" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK",  content = {@Content(schema = @Schema(ref = "#/components/schemas/Map"))}),
            @ApiResponse(responseCode = "400", description = "Bad Request",   content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @GetMapping("/{partitionId}")
    @PreAuthorize("@authorizationFilter.hasPermissions()")
    public ResponseEntity<Map<String, Property>> get(@Parameter(description = "Partition Id") @PathVariable("partitionId") String partitionId) {
        PartitionInfo partitionInfo = this.partitionService.getPartition(partitionId);
        this.auditLogger.readPartitionSuccess(Collections.singletonList(partitionId));
        return ResponseEntity.ok(partitionInfo.getProperties());
    }

    @Operation(summary = "${partitionApi.delete.summary}", description = "${partitionApi.delete.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "partition-api" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content"),
            @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @DeleteMapping("/{partitionId}")
    @PreAuthorize("@authorizationFilter.hasPermissions()")
    public ResponseEntity delete(@Parameter(description = "Partition Id") @PathVariable("partitionId") String partitionId) {
        this.partitionService.deletePartition(partitionId);
        this.auditLogger.deletePartitionSuccess(Collections.singletonList(partitionId));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "${partitionApi.list.summary}", description = "${partitionApi.list.description}",
            security = {@SecurityRequirement(name = "Authorization")}, tags = { "partition-api" })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = { @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))}),
            @ApiResponse(responseCode = "400", description = "Bad Request",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "403", description = "User not authorized to perform the action.",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "404", description = "Not Found",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "502", description = "Bad Gateway",  content = {@Content(schema = @Schema(implementation = AppError.class ))}),
            @ApiResponse(responseCode = "503", description = "Service Unavailable",  content = {@Content(schema = @Schema(implementation = AppError.class ))})
    })
    @GetMapping
    @PreAuthorize("@authorizationFilter.hasPermissions()")
    public List<String> list() {
        List<String> partitions = this.partitionService.getAllPartitions();
        this.auditLogger.readListPartitionSuccess(
            Collections.singletonList(String.format("Partition list size = %s", partitions.size())));
        return partitions;
    }
}
