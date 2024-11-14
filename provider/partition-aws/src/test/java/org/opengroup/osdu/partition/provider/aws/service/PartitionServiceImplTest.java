//// Copyright © 2020 Amazon Web Services
//// Copyright 2017-2020, Schlumberger
////
//// Licensed under the Apache License, Version 2.0 (the "License");
//// you may not use this file except in compliance with the License.
//// You may obtain a copy of the License at
////
////      http://www.apache.org/licenses/LICENSE-2.0
////
//// Unless required by applicable law or agreed to in writing, software
//// distributed under the License is distributed on an "AS IS" BASIS,
//// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//// See the License for the specific language governing permissions and
//// limitations under the License.


package org.opengroup.osdu.partition.provider.aws.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.aws.dynamodb.DynamoDBQueryHelperV2;
import org.opengroup.osdu.core.aws.dynamodb.IDynamoDBQueryHelperFactory;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.aws.config.ProviderConfigurationBag;
import org.opengroup.osdu.partition.provider.aws.model.PartitionDoc;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartitionServiceImplTest {

    private static final String PARTITION_ID = "test-partition";
    private static final String REGION = "us-east-1";
    private static final String TABLE_NAME = "test-table";

    @Mock
    private JaxRsDpsLog logger;

    @Mock
    private DynamoDBQueryHelperV2 queryHelper;

    @Mock
    private IDynamoDBQueryHelperFactory queryHelperFactory;

    @Captor
    private ArgumentCaptor<PartitionDoc> partitionDocCaptor;

    private PartitionServiceImpl partitionService;
    private PartitionInfo partitionInfo;
    private ProviderConfigurationBag config;

    @BeforeEach
    void setUp() {
        config = new ProviderConfigurationBag();
        config.amazonRegion = REGION;
        config.dynamodbTableName = TABLE_NAME;

        when(queryHelperFactory.getQueryHelper(REGION, TABLE_NAME)).thenReturn(queryHelper);

        partitionService = new PartitionServiceImpl(queryHelperFactory, logger, config);

        // Setup test partition info
        Map<String, Property> properties = new HashMap<>();
        properties.put("storageAccount", Property.builder()
                .value("test-storage")
                .sensitive(false)
                .build());
        partitionInfo = PartitionInfo.builder()
                .properties(properties)
                .build();
    }

    @Test
    void createPartition_Success() {
        // Arrange
        when(queryHelper.keyExistsInTable(eq(PartitionDoc.class), any())).thenReturn(false);

        // Act
        PartitionInfo result = partitionService.createPartition(PARTITION_ID, partitionInfo);

        // Assert
        assertNotNull(result);
        verify(queryHelper).save(partitionDocCaptor.capture());
        assertEquals(PARTITION_ID, partitionDocCaptor.getValue().getId());
        assertEquals(partitionInfo, partitionDocCaptor.getValue().getPartitionInfo());
    }

    @Test
    void createPartition_ThrowsException_WhenPartitionExists() {
        // Arrange
        when(queryHelper.keyExistsInTable(eq(PartitionDoc.class), any())).thenReturn(true);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> partitionService.createPartition(PARTITION_ID, partitionInfo));
        assertEquals(409, exception.getError().getCode());
        assertEquals("partition exist", exception.getError().getReason());
    }

    @Test
    void updatePartition_Success() {
        // Arrange
        PartitionDoc existingDoc = PartitionDoc.create(PARTITION_ID, partitionInfo);
        when(queryHelper.loadByPrimaryKey(PartitionDoc.class, PARTITION_ID)).thenReturn(existingDoc);

        // Act
        PartitionInfo result = partitionService.updatePartition(PARTITION_ID, partitionInfo);

        // Assert
        assertNotNull(result);
        verify(queryHelper).save(any(PartitionDoc.class));
    }

    @Test
    void updatePartition_ThrowsException_WhenPartitionNotFound() {
        // Arrange
        when(queryHelper.loadByPrimaryKey(PartitionDoc.class, PARTITION_ID)).thenReturn(null);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> partitionService.updatePartition(PARTITION_ID, partitionInfo));
        assertEquals(404, exception.getError().getCode());
    }

    @Test
    void updatePartition_ThrowsException_WhenIdUpdateAttempted() {
        // Arrange
        Map<String, Property> properties = new HashMap<>();
        properties.put("id", Property.builder().value("new-id").build());
        PartitionInfo invalidInfo = PartitionInfo.builder().properties(properties).build();

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> partitionService.updatePartition(PARTITION_ID, invalidInfo));
        assertEquals(400, exception.getError().getCode());
    }

    @Test
    void getPartition_Success() {
        // Arrange
        PartitionDoc existingDoc = PartitionDoc.create(PARTITION_ID, partitionInfo);
        when(queryHelper.loadByPrimaryKey(PartitionDoc.class, PARTITION_ID)).thenReturn(existingDoc);

        // Act
        PartitionInfo result = partitionService.getPartition(PARTITION_ID);

        // Assert
        assertNotNull(result);
        assertEquals(partitionInfo, result);
    }

    @Test
    void getPartition_ThrowsException_WhenNotFound() {
        // Arrange
        when(queryHelper.loadByPrimaryKey(PartitionDoc.class, PARTITION_ID)).thenReturn(null);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> partitionService.getPartition(PARTITION_ID));
        assertEquals(404, exception.getError().getCode());
    }

    @Test
    void deletePartition_Success() {
        // Arrange
        PartitionDoc existingDoc = PartitionDoc.create(PARTITION_ID, partitionInfo);
        when(queryHelper.loadByPrimaryKey(PartitionDoc.class, PARTITION_ID)).thenReturn(existingDoc);

        // Act
        boolean result = partitionService.deletePartition(PARTITION_ID);

        // Assert
        assertTrue(result);
        verify(queryHelper).deleteByPrimaryKey(PartitionDoc.class, PARTITION_ID);
    }

    @Test
    void deletePartition_ThrowsException_WhenNotFound() {
        // Arrange
        when(queryHelper.loadByPrimaryKey(PartitionDoc.class, PARTITION_ID)).thenReturn(null);

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> partitionService.deletePartition(PARTITION_ID));
        assertEquals(404, exception.getError().getCode());
    }

    @Test
    void getAllPartitions_Success() {
        // Arrange
        ArrayList<PartitionDoc> partitions = new ArrayList<>();
        partitions.add(PartitionDoc.create("partition1", partitionInfo));
        partitions.add(PartitionDoc.create("partition2", partitionInfo));

        when(queryHelper.scanTable(PartitionDoc.class)).thenReturn(partitions);

        // Act
        List<String> result = partitionService.getAllPartitions();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("partition1"));
        assertTrue(result.contains("partition2"));
    }

    @Test
    void getAllPartitions_ThrowsException_WhenQueryHelperFails() {
        // Arrange
        String errorMessage = "DynamoDB scan operation failed";
        when(queryHelper.scanTable(PartitionDoc.class))
                .thenThrow(new RuntimeException(errorMessage));

        // Act & Assert
        AppException exception = assertThrows(AppException.class,
                () -> partitionService.getAllPartitions());

        // Verify exception details
        assertEquals(500, exception.getError().getCode());
        assertEquals("Failed to retrieve partitions", exception.getError().getReason());
        assertEquals(errorMessage, exception.getError().getMessage());

        // Verify logging
        verify(logger).error(eq("Failed to retrieve all partitions"), any(RuntimeException.class));
    }
}
