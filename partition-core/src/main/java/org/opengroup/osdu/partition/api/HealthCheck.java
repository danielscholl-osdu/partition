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

import org.opengroup.osdu.partition.logging.AuditLogger;
import org.opengroup.osdu.partition.provider.interfaces.IHealthCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping(path = "/_ah", produces = "application/json")
public class HealthCheck {

    @Autowired
    private AuditLogger auditLogger;
    @Autowired
    private IHealthCheckService healthCheckService;

    @GetMapping("/liveness_check")
    public ResponseEntity<String> livenessCheck() {
        healthCheckService.performLivenessCheck();
        ResponseEntity responseEntity = new ResponseEntity<>("Partition service is alive", HttpStatus.OK);
        this.auditLogger.readServiceLivenessSuccess(Collections.singletonList(responseEntity.toString()));
        return responseEntity;
    }

    @GetMapping("/readiness_check")
    public ResponseEntity<String> readinessCheck() {
        healthCheckService.performReadinessCheck();
        return new ResponseEntity<>("Partition service is ready", HttpStatus.OK);
    }
}
