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

package org.opengroup.osdu.partition.provider.azure.service;

import com.azure.security.keyvault.secrets.SecretClient;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.azure.utils.KeyVaultFacade;
import org.opengroup.osdu.partition.provider.azure.utils.ThreadPoolService;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PartitionServiceImpl implements IPartitionService {

    @Autowired
    private SecretClient secretClient;
    @Autowired
    private ThreadPoolService threadPoolService;

    private static final String APP_DEV_SP_USERNAME = "app-dev-sp-username";
    private static final String SERVICE_PRINCIPAL_ID = "sp-appid";

    @Override
    public PartitionInfo createPartition(String partitionId, PartitionInfo partitionInfo) {
        if (this.partitionExists(partitionId)) {
            throw new AppException(HttpStatus.SC_CONFLICT, "partition exist", "Partition with same id exist");
        }
        this.addTenantSecretes(partitionId, partitionInfo);

        return partitionInfo;
    }

    @Override
    public PartitionInfo getPartition(String partitionId) {
        if (!this.partitionExists(partitionId)) {
            throw new AppException(HttpStatus.SC_NOT_FOUND, "partition not found", String.format("%s partition not found", partitionId));
        }

        Map<String, Object> out = new HashMap<>();
        out.putAll(this.getTenantSecreteInfo(partitionId));

        return PartitionInfo.builder().properties(out).build();
    }

    @Override
    public boolean deletePartition(String partitionId) {
        if (!this.partitionExists(partitionId)) {
            throw new AppException(HttpStatus.SC_NOT_FOUND, "partition not found", String.format("%s partition not found", partitionId));
        }

        this.deleteTenantSecrets(partitionId);

        return true;
    }

    private void addTenantSecretes(String partitionId, PartitionInfo partitionInfo) {
        // id
        KeyVaultFacade.createKeyVaultSecret(this.secretClient, getTenantSafeSecreteId(partitionId, "id"), partitionId);

        // rest of keys
        for (Map.Entry<String, Object> entry : partitionInfo.getProperties().entrySet()) {
            String secreteName = this.getTenantSafeSecreteId(partitionId, entry.getKey());
            KeyVaultFacade.createKeyVaultSecret(this.secretClient, secreteName, String.valueOf(entry.getValue()));
        }
    }

    private Map<String, Object> getTenantSecreteInfo(String partitionId) {
        Map<String, Object> out = new HashMap<>();
        List<String> secreteKeys = KeyVaultFacade.getKeyVaultSecrets(secretClient, partitionId);
        if (secreteKeys.isEmpty()) {
            return out;
        }

        for (String key : secreteKeys) {
            String outKey = key.replaceFirst(String.format("%s-", partitionId), "");
            out.put(outKey, KeyVaultFacade.getKeyVaultSecret(this.secretClient, key));
        }
        out.put(SERVICE_PRINCIPAL_ID, KeyVaultFacade.getKeyVaultSecret(this.secretClient, APP_DEV_SP_USERNAME));
        return out;
    }

    private void deleteTenantSecrets(String partitionId) {
        List<String> secreteKeys = KeyVaultFacade.getKeyVaultSecrets(secretClient, partitionId);
        if (secreteKeys.isEmpty()) {
            return;
        }

        this.threadPoolService.createDeletePoolIfNeeded(secreteKeys.size());

        for (String key : secreteKeys) {
            this.threadPoolService.getExecutorService().submit(() -> KeyVaultFacade.deleteKeyVaultSecret(this.secretClient, key));
        }
    }

    private String getTenantSafeSecreteId(String partitionId, String secreteName) {
        return String.format("%s-%s", partitionId, secreteName);
    }

    private boolean partitionExists(String partitionId) {
        return KeyVaultFacade.secretExists(secretClient, getTenantSafeSecreteId(partitionId, "id"));
    }
}