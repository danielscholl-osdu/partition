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

package org.opengroup.osdu.partition.provider.aws.service;

import java.util.List;
import java.util.Map;

import com.amazonaws.partitions.model.Partition;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.aws.util.SSMHelper;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PartitionServiceImpl implements IPartitionService {

    @Autowired
    private JaxRsDpsLog logger;
    
    @Autowired
    private SSMHelper ssmHelper;

    public PartitionServiceImpl() {
        
    }

    @Override
    public PartitionInfo createPartition(String partitionId, PartitionInfo partitionInfo) {
        
        if (ssmHelper.partitionExists(partitionId)) {
            throw new AppException(HttpStatus.SC_CONFLICT, "partition exist", "Partition with same id exist");
        }

        try {
            for (Map.Entry<String, Property> entry : partitionInfo.getProperties().entrySet()) {
                ssmHelper.createOrUpdateSecret(partitionId, entry.getKey(), entry.getValue());
            }

            /** 
            *   SSM parameters are not immediately available after pushing to System Manager.
            *   This API is expected to return a 200 response meaning that the parameters should be available immediately.
            *   This logic is added to validate when the parameters become available before returning the 200 response.
            *   The performance hit is acceptable because partitions are only created as an early operation and shouldn't affect
            *   the performance of runtime workflows             
            */
            int retryCount = 10;
            boolean partitionReady = false;
            while (!partitionReady && retryCount > 0) {
                retryCount--;
                List<String> partitionCheck = ssmHelper.getSsmParamsPathsForPartition(partitionId);
                if (partitionCheck.size() == partitionInfo.getProperties().size())
                    partitionReady = true;
                else
                    Thread.sleep(500);
            }

            String rollbackSuccess = "Failed";
            if (!partitionReady) {
                try {
                    ssmHelper.deletePartitionSecrets(partitionId);
                    rollbackSuccess = "Succeeded";
                }
                catch (Exception e){ 

                }

                throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Partition Creation Failed", "One or more secrets couldn't be stored. Rollback " + rollbackSuccess);
                
            }
        }
        catch (AppException appE) {
            throw appE;
        }
        catch (Exception e) {

            try {
                Thread.sleep(2000); //wait for any existing ssm parameters that got added to normalize
                ssmHelper.deletePartitionSecrets(partitionId);
            }
            catch (Exception deleteE) {                
                //if the partition didnt get created at all deletePartition will throw an exception. Eat it so we return the creation exception.
            }

            logger.error("Failed to create partition due to key creation failure in ssm", e.getMessage());
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Partition Creation Failure", e.getMessage(), e);
        }
        
        return partitionInfo;
    }

    @Override
    public PartitionInfo getPartition(String partitionId) {

        Map<String,Property> secrets = ssmHelper.getPartitionSecrets(partitionId);


        //throw error if partition doesn't exist
        if (secrets.size() <= 0) {
            throw new AppException(HttpStatus.SC_NOT_FOUND, "partition not found", String.format("%s partition not found", partitionId));
        }
        
        return PartitionInfo.builder().properties(secrets).build();
    }

    @Override
    public boolean deletePartition(String partitionId) {
        
        if (!ssmHelper.partitionExists(partitionId)) {
            throw new AppException(HttpStatus.SC_NOT_FOUND, "partition not found", String.format("%s partition not found", partitionId));
        }

        return ssmHelper.deletePartitionSecrets(partitionId);
    }

    @Override
    public List<String> getAllPartitions() {
        //TODO: Pending to be implemented
        return null;
    }

}
