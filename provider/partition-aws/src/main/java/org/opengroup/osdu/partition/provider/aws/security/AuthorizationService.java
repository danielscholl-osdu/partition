// Copyright © 2020 Amazon Web Services
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

package org.opengroup.osdu.partition.provider.aws.security;

import org.opengroup.osdu.core.common.entitlements.IEntitlementsAndCacheService;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.partition.provider.interfaces.IAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class AuthorizationService implements IAuthorizationService {

    public static final java.lang.String PARTITION_GROUP = "reserved_aws_admin";
    public static final java.lang.String PARTITION_ADMIN_ROLE = "service.partition.admin";

    @Autowired
    private IEntitlementsAndCacheService entitlementsAndCacheService;
    
    @Autowired
    private DpsHeaders headers;

    @Override
    public boolean isDomainAdminServiceAccount() {
        return hasRole(PARTITION_ADMIN_ROLE);
    }

    private boolean hasRole(String requiredRole) {        
        headers.put(DpsHeaders.DATA_PARTITION_ID, PARTITION_GROUP);
        String user = entitlementsAndCacheService.authorize(headers, requiredRole);
        headers.put(DpsHeaders.USER_EMAIL, user);
        return true;
    }

}

