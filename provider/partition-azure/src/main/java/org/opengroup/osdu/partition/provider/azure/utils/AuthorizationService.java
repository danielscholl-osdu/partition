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

package org.opengroup.osdu.partition.provider.azure.utils;

import com.microsoft.azure.spring.autoconfigure.aad.UserPrincipal;
import org.opengroup.osdu.partition.provider.interfaces.IAuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationService implements IAuthorizationService {

    enum UserType {
        REGULAR_USER,
        GUEST_USER,
        SERVICE_PRINCIPAL
    }

    @Override
    public boolean isDomainAdminServiceAccount() {
        final Object principal = getUserPrincipal();

        if (!(principal instanceof UserPrincipal)) {
            return false;
        }

        final UserPrincipal userPrincipal = (UserPrincipal) principal;

        UserType type = getType(userPrincipal);
        if (type == UserType.SERVICE_PRINCIPAL) {
            return true;
        }
        return false;
    }

    /**
     * The internal method to get the user principal.
     *
     * @return user principal
     */
    private Object getUserPrincipal() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getPrincipal();
    }

    /**
     * Convenience method returning the type of user
     *
     * @param u user principal to check
     * @return the user type
     */
    private UserType getType(UserPrincipal u) {
        UserType type;
        if (u.getUpn() != null) {
            type = UserType.REGULAR_USER;
        } else if (u.getUniqueName() != null) {
            type = UserType.GUEST_USER;
        } else {
            type = UserType.SERVICE_PRINCIPAL;
        }
        return type;
    }
}
