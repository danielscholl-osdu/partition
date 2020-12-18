/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.partition.provider.ibm.security;

import org.opengroup.osdu.core.common.entitlements.IEntitlementsAndCacheService;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.partition.provider.interfaces.IAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class AuthorizationService implements IAuthorizationService {

    public static final String PARTITION_ADMIN_ROLE = "service.partition.admin";

    @Autowired
    private IEntitlementsAndCacheService entitlementsAndCacheService;
    
    @Autowired
    private DpsHeaders headers;

    @Override
    public boolean isDomainAdminServiceAccount() {
        try {
            return hasRole(PARTITION_ADMIN_ROLE);
        }
        catch (AppException e) {
            throw e;
        }
        catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Authentication Failure", e.getMessage(), e);
        }
        
    }

    private boolean hasRole(String requiredRole) {        
        //headers.put(DpsHeaders.DATA_PARTITION_ID, PARTITION_NAME);
        String user = entitlementsAndCacheService.authorize(headers, requiredRole);
        headers.put(DpsHeaders.USER_EMAIL, user);
        return true;
    }

}

