/*
  Copyright 2002-2021 Google LLC
  Copyright 2002-2021 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.opengroup.osdu.partition.provider.gcp.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.partition.provider.gcp.config.PropertiesConfiguration;
import org.opengroup.osdu.partition.provider.interfaces.IAuthorizationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Slf4j
@Component
@RequestScope
@RequiredArgsConstructor
@ConditionalOnProperty(name = "environment", havingValue = "gcp")
public class GcpAuthorizationService implements IAuthorizationService {

  private final PropertiesConfiguration configuration;

  private final DpsHeaders headers;

  private final GoogleIdTokenVerifier verifier;

  @Override
  public boolean isDomainAdminServiceAccount() {
    if (Objects.isNull(headers.getAuthorization()) || headers.getAuthorization().isEmpty()) {
      throw AppException.createUnauthorized("No JWT token. Access is Forbidden");
    }
    String email = null;
    try {
      String authorization = headers.getAuthorization().replace("Bearer ", "");
      GoogleIdToken googleIdToken = verifier.verify(authorization);
      if (Objects.isNull(googleIdToken)) {
        log.warn("Not valid token provided");
        throw AppException.createUnauthorized("Unauthorized. The JWT token could not be validated");
      }
      email = googleIdToken.getPayload().getEmail();
      List<String> partitionAdminAccounts = configuration.getPartitionAdminAccounts();
      if (Objects.nonNull(partitionAdminAccounts) && !partitionAdminAccounts.isEmpty()) {
        return isAllowedAccount(email);
      } else {
        if (StringUtils.endsWith(email, configuration.getServiceAccountTail())) {
          return true;
        } else {
          throw AppException.createUnauthorized(
              String.format("Unauthorized. The user %s is not Service Principal", email));
        }
      }
    } catch (AppException e){
      throw e;
    } catch (Exception ex) {
      log.warn(String.format("User %s unauthorized. %s.", email, ex));
      throw AppException.createUnauthorized("Unauthorized. The JWT token could not be validated");
    }
  }

  private boolean isAllowedAccount(String accountEmail) {
    for (String partitionAdmin : configuration.getPartitionAdminAccounts()) {
      if (partitionAdmin.equals(accountEmail)) {
        return true;
      }

    if (StringUtils.endsWith(accountEmail, configuration.getServiceAccountTail())) {
        if (StringUtils.startsWith(accountEmail, partitionAdmin)) {
          return true;
        }
      }
    }
    throw AppException
        .createUnauthorized(String.format("Unauthorized. The user %s is untrusted.", accountEmail));
  }
}
