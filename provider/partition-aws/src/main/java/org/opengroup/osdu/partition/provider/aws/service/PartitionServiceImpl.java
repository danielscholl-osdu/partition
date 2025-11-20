/*
 * Copyright © Amazon Web Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.partition.provider.aws.service;

import java.util.*;

import org.apache.http.HttpStatus;
import org.bson.types.Binary;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.aws.model.Partition;
import org.opengroup.osdu.partition.provider.aws.model.IPartitionRepository;
import org.opengroup.osdu.partition.provider.aws.util.AwsKmsEncryptionClient;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * AWS implementation doesn't use cache.
 */
@Service
public class PartitionServiceImpl implements IPartitionService {

    private final JaxRsDpsLog logger;
    private final IPartitionRepository repository;
    private final AwsKmsEncryptionClient awsKmsEncryptionClient;

    public PartitionServiceImpl(JaxRsDpsLog logger, 
                               IPartitionRepository repository, 
                               AwsKmsEncryptionClient awsKmsEncryptionClient) {
        this.logger = logger;
        this.repository = repository;
        this.awsKmsEncryptionClient = awsKmsEncryptionClient;
    }

    @Override
    public PartitionInfo createPartition(String partitionId, PartitionInfo partitionInfo) {

        // throw error if partition already exists
        if (repository.findById(partitionId).isPresent()) {
            throw new AppException(HttpStatus.SC_CONFLICT, "partition exist", "Partition with same id exist");
        }

        Partition savedPartition = repository.save(new Partition(partitionId, encryptSensitiveProperties(partitionInfo, partitionId)));

        return new PartitionInfo(savedPartition.getProperties());
    }

    @Override
    public PartitionInfo updatePartition(String partitionId, PartitionInfo partitionInfo) {

        Optional<Partition> partition = repository.findById(partitionId);

        // throw error if search did not return a result
        if (!partition.isPresent()) {
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Partition does not exist", "Partition does not exist");
        }

        // throw error if the client tries to update the id
        if (partitionInfo.getProperties().containsKey("id")) {
            throw new AppException(HttpStatus.SC_BAD_REQUEST, "Cannot update id", "the field id cannot be updated");
        }

        Map<String, Property> updatedProperties = partition.get().getProperties();
        Map<String, Property> encryptedPropertiesToAdd = encryptSensitiveProperties(partitionInfo, partitionId);

        for (Map.Entry<String, Property> e : encryptedPropertiesToAdd.entrySet()) {
            updatedProperties.put(e.getKey(), e.getValue());
        }

        // throw error if save was unsuccessful
        try {
            repository.save(new Partition(partitionId, updatedProperties));
        } catch (Exception e) {
            logger.error("Failed to update partition", e.getMessage());
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Partition update Failure", e.getMessage(), e);
        }

        return partitionInfo;
    }

    @Override
    public PartitionInfo getPartition(String partitionId) {

        Optional<Partition> partition = repository.findById(partitionId);

        // throw error if partition doesn't exist
        if (!partition.isPresent()) {
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Partition not found", String.format("%s partition not found", partitionId));
        }

        PartitionInfo partitionInfo;

        try {
            partitionInfo = decryptSensitiveProperties(partition.get().getProperties(), partitionId);
        } catch (ClassCastException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Corrupt data", String.format("%s contains unreadable data", partitionId));
        } catch (IllegalStateException e) {
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Illegal database modification", String.format("Partition %s has been modified without permission", partitionId));
        }

        return partitionInfo;

    }

    @Override
    public boolean deletePartition(String partitionId) {

        // throw error if partition doesn't exist
        if (!repository.findById(partitionId).isPresent()) {
            throw new AppException(HttpStatus.SC_NOT_FOUND, "Partition not found", String.format("%s partition not found", partitionId));
        }

        repository.deleteById(partitionId);

        return true;
    }

    @Override
    public List<String> getAllPartitions() {

        List<Partition> partitionList = repository.findAll();

        List<String> partitions = new ArrayList<>();

        // populate list of ids, i.e. partition names
        for (Partition p: partitionList) {
            String id = p.getId();
            partitions.add(id);
        }

        return partitions;
    }

    private Map<String, Property> encryptSensitiveProperties(PartitionInfo partitionInfo, String id) {

        Map<String, Property> encryptedProperties = new HashMap<>();

        // encrypt all properties that are flagged as sensitive
        for (Map.Entry<String, Property> e : partitionInfo.getProperties().entrySet()) {

            Property encryptedProp = new Property();
            encryptedProp.setSensitive(e.getValue().isSensitive());

            if (encryptedProp.isSensitive()) {
                encryptedProp.setValue(awsKmsEncryptionClient.encrypt(e.getValue().getValue().toString(), id));
            } else {
                encryptedProp.setValue(e.getValue().getValue());
            }

            encryptedProperties.put(e.getKey(), encryptedProp);
        }

        return encryptedProperties;
    }

    private PartitionInfo decryptSensitiveProperties(Map<String, Property> properties, String id) throws ClassCastException {

        HashMap<String,Property> decryptedProperties = new HashMap<>();

        // decrypt all properties that are flagged as sensitive
        for (Map.Entry<String, Property> e : properties.entrySet()) {

            Property decryptedProp = new Property();
            decryptedProp.setSensitive(e.getValue().isSensitive());

            if (decryptedProp.isSensitive()) {
                Binary bin = (Binary) e.getValue().getValue();
                decryptedProp.setValue(awsKmsEncryptionClient.decrypt(bin.getData(), id));
            } else {
                decryptedProp.setValue(e.getValue().getValue());
            }

            decryptedProperties.put(e.getKey(), decryptedProp);
        }

        return new PartitionInfo(decryptedProperties);
    }
}
