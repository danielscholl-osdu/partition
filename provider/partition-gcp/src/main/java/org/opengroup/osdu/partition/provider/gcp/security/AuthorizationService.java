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
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.util.Collections;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.partition.provider.gcp.config.PropertiesConfiguration;
import org.opengroup.osdu.partition.provider.interfaces.IAuthorizationService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Slf4j
@Component
@RequestScope
@RequiredArgsConstructor
public class AuthorizationService implements IAuthorizationService {

    private final PropertiesConfiguration configuration;

    private final DpsHeaders headers;

    @Override
    public boolean isDomainAdminServiceAccount() {
        try {
            GoogleIdTokenVerifier verifier =
                new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singleton(configuration.getGoogleAudiences()))
                    .build();

            String authorization = headers.getAuthorization().replace("Bearer ", "");
            GoogleIdToken googleIdToken = verifier.verify(authorization);
            if (Objects.isNull(googleIdToken)) {
                log.warn("Not valid token provided");
                return false;
            }
            String email = googleIdToken.getPayload().getEmail();
            String partitionAdminAccount = configuration.getPartitionAdminAccount();
            if (Objects.nonNull(partitionAdminAccount) && !partitionAdminAccount.isEmpty()) {
                return email.equals(partitionAdminAccount);
            }
            return StringUtils.endsWithIgnoreCase(email, "gserviceaccount.com");
        } catch (Exception e) {
            log.warn("Not valid or expired token provided");
            return false;
        }
    }
}
