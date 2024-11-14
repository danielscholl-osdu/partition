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
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperV2;
import org.opengroup.osdu.core.aws.dynamodb.IDynamoDBQueryHelperFactory;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.partition.provider.aws.model.PartitionDoc;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of IPartitionService for AWS DynamoDB.
 * Handles CRUD operations for partitions in DynamoDB.
 */
@Service
public class PartitionServiceImpl implements IPartitionService {

    private final JaxRsDpsLog logger;
    private final IPartitionRepository repository;
    private static final String PARTITION_NOT_FOUND = "Partition not found";
    private static final String PARTITION_EXISTS = "Partition with same id exists";
    private static final String CANNOT_UPDATE_ID = "The field id cannot be updated";

    private final JaxRsDpsLog logger;
    private final DynamoDBQueryHelperV2 queryHelper;

    @Autowired
    public PartitionServiceImpl(IDynamoDBQueryHelperFactory queryHelperFactory,
                                JaxRsDpsLog logger,
                                ProviderConfigurationBag config) {
        this.logger = logger;
        this.queryHelper = queryHelperFactory.getQueryHelper(
                config.amazonRegion,
                config.dynamodbTableName
        );
    }

    public PartitionServiceImpl(JaxRsDpsLog logger, 
                               IPartitionRepository repository, 
                               AwsKmsEncryptionClient awsKmsEncryptionClient) {
        this.logger = logger;
        this.repository = repository;
        this.awsKmsEncryptionClient = awsKmsEncryptionClient;
    }

    @Override
    public PartitionInfo createPartition(String partitionId, PartitionInfo partitionInfo) {

        PartitionDoc partition = PartitionDoc.create(partitionId, partitionInfo);

        if (queryHelper.keyExistsInTable(PartitionDoc.class, partition)) {
            logger.error("Attempted to create duplicate partition: " + partitionId);
            throw new AppException(HttpStatus.SC_CONFLICT, "partition exist", PARTITION_EXISTS);
        }

        try {
            queryHelper.save(partition);
            logger.info("Created partition: " + partitionId);
            return partition.getPartitionInfo();
        } catch (Exception e) {
            logger.error("Failed to create partition: " + partitionId, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Failed to create partition", e.getMessage());
        }
    }

    @Override
    public PartitionInfo updatePartition(String partitionId, PartitionInfo partitionInfo) {
        validateInput(partitionId, partitionInfo);
        validateNoIdUpdate(partitionInfo);

        PartitionDoc partition = getPartitionOrThrow(partitionId);

        try {
            PartitionInfo updatedProperties = partition.getPartitionInfo();
            queryHelper.save(PartitionDoc.create(partitionId, updatedProperties));
            logger.info("Updated partition: " + partitionId);
            return partitionInfo;
        } catch (Exception e) {
            logger.error("Failed to update partition: " + partitionId, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Failed to update partition", e.getMessage());
        }
    }

    @Override
    public PartitionInfo getPartition(String partitionId) {
        validatePartitionId(partitionId);
        return getPartitionOrThrow(partitionId).getPartitionInfo();
    }

    @Override
    public boolean deletePartition(String partitionId) {
        validatePartitionId(partitionId);
        getPartitionOrThrow(partitionId); // Verify existence

        try {
            queryHelper.deleteByPrimaryKey(PartitionDoc.class, partitionId);
            logger.info("Deleted partition: " + partitionId);
            return true;
        } catch (Exception e) {
            logger.error("Failed to delete partition: " + partitionId, e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Failed to delete partition", e.getMessage());
        }
    }

    @Override
    public List<String> getAllPartitions() {
        try {
            return queryHelper.scanTable(PartitionDoc.class).stream()
                    .map(PartitionDoc::getId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to retrieve all partitions", e);
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Failed to retrieve partitions", e.getMessage());
        }
    }

    private void validateInput(String partitionId, PartitionInfo partitionInfo) {
        validatePartitionId(partitionId);
        if (partitionInfo == null) {
            throw new IllegalArgumentException("PartitionInfo cannot be null");
        }
    }

    private void validatePartitionId(String partitionId) {
        if (partitionId == null || partitionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Partition ID cannot be null or empty");
        }
    }

    private void validateNoIdUpdate(PartitionInfo partitionInfo) {
        if (partitionInfo.getProperties().containsKey("id")) {
            throw new AppException(HttpStatus.SC_BAD_REQUEST,
                    "Cannot update id", CANNOT_UPDATE_ID);
        }
    }

    private PartitionDoc getPartitionOrThrow(String partitionId) {
        PartitionDoc partition = queryHelper.loadByPrimaryKey(PartitionDoc.class, partitionId);
        if (partition == null) {
            logger.error("Partition not found: " + partitionId);
            throw new AppException(HttpStatus.SC_NOT_FOUND, PARTITION_NOT_FOUND,
                    String.format("%s partition not found", partitionId));
        }
        return partition;
    }
}
