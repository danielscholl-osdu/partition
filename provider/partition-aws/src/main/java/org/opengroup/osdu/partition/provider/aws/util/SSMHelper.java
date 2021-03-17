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

package org.opengroup.osdu.partition.provider.aws.util;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.*;
import org.opengroup.osdu.core.aws.iam.IAMConfig;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.aws.AwsServiceConfig;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Component
public final class SSMHelper {

    @Inject
    private AwsServiceConfig awsServiceConfig;

    private AWSCredentialsProvider amazonAWSCredentials;
    private AWSSimpleSystemsManagement ssmManager;

    public SSMHelper() {
        amazonAWSCredentials = IAMConfig.amazonAWSCredentials();
        ssmManager = AWSSimpleSystemsManagementClientBuilder.standard()
                .withCredentials(amazonAWSCredentials)
                .build();
    }

    // public boolean secretExists(String secretName) {
    //     GetParameterRequest request = new GetParameterRequest().withName(secretName);
    //     GetParameterResult result = ssmManager.getParameter(request);

    //     return result.getParameter() != null;
    // }

    private String getSsmPathForPartitition(String partitionName) {
        return URI.create(getTenantPrefix() + '/' + partitionName + '/').normalize().toString();
    }

    private String getSsmPathForPartititions() {
        return URI.create(getTenantPrefix() + '/').normalize().toString();
    }

    private String getSsmPathForPartititionSecret(String partitionName, String secretName) {
        return URI.create(getSsmPathForPartitition(partitionName) + '/' + secretName).normalize().toString();
    }

    private List<Parameter> getSsmParamsForPartition(String partitionName) {

        String ssmPath = getSsmPathForPartitition(partitionName);

        List<Parameter> params = new ArrayList<Parameter>();
        String nextToken = null;

        do {

            GetParametersByPathRequest request = new GetParametersByPathRequest()
                    .withPath(ssmPath)
                    .withRecursive(true)
                    .withNextToken(nextToken)
                    .withWithDecryption(true);

            GetParametersByPathResult result = ssmManager.getParametersByPath(request);
            nextToken = result.getNextToken();

            if (result.getParameters().size() > 0)
                params.addAll(result.getParameters());
        }
        while (nextToken != null);

        return params;
    }

    public List<String> getSsmParamsPathsForPartition(String partitionName) {

        List<Parameter> paramsToDelete = getSsmParamsForPartition(partitionName);

        List<String> ssmParamNames = paramsToDelete.stream().map(Parameter::getName).collect(Collectors.toList());

        return ssmParamNames;
    }

    public boolean partitionExists(String partitionName) {

        String ssmPath = getSsmPathForPartitition(partitionName);
        String nextToken = null;

        do {

            GetParametersByPathRequest request = new GetParametersByPathRequest()
                    .withPath(ssmPath)
                    .withRecursive(true)
                    .withNextToken(nextToken);

            GetParametersByPathResult result = ssmManager.getParametersByPath(request);
            nextToken = result.getNextToken();

            if (result.getParameters().size() > 0)
                return true;
        }
        while (nextToken != null);

        return false;
    }

    public Map<String, Property> getPartitionSecrets(String partitionName) {

        List<Parameter> partitionSsmParameters = getSsmParamsForPartition(partitionName);

        String ssmPath = getSsmPathForPartitition(partitionName);

        Map<String, Property> kvMap = new HashMap<>();

        for (Parameter parameter : partitionSsmParameters) {

            String shortName = parameter.getName().substring(ssmPath.length());

            kvMap.put(shortName, Property.builder().value(parameter.getValue()).build());
        }

        return kvMap;
    }

    public boolean createOrUpdateSecret(String partitionName, String secretName, Object secretValue) {

        String ssmPath = getSsmPathForPartititionSecret(partitionName, secretName);

        PutParameterRequest request = new PutParameterRequest()
                .withName(ssmPath)
                .withType(ParameterType.SecureString)
                .withOverwrite(true)
                .withValue(String.valueOf(secretValue));


        PutParameterResult result = ssmManager.putParameter(request);

        //secret creation throws an exception if there's an error so we wont hit here
        return true;

    }

    public boolean deletePartitionSecrets(String partitionName) {

        List<String> ssmParamPaths =  getSsmParamsPathsForPartition(partitionName);

        int expectedNumOfDeletedParams = ssmParamPaths.size();
        int totalDeletedParams = 0;

        while (ssmParamPaths.size() > 0) {
            int subListCount = ssmParamPaths.size();
            if (subListCount > 10)
                subListCount = 10;

            List<String> paramsToDelete = ssmParamPaths.subList(0, subListCount);
            ssmParamPaths = ssmParamPaths.subList(subListCount, ssmParamPaths.size());

            DeleteParametersRequest request = new DeleteParametersRequest()
                    .withNames(paramsToDelete);

            DeleteParametersResult result =  ssmManager.deleteParameters(request);

            totalDeletedParams += result.getDeletedParameters().size();
        }



        return totalDeletedParams == expectedNumOfDeletedParams;

    }

    public List<String> getPartitions() {

        List<String> partitions = new ArrayList<>();
        Set<String> uniquePartitions = new HashSet<String>();
        String ssmPath = getSsmPathForPartititions();

        String nextToken = null;
        GetParametersByPathResult result=null;
        do {

            GetParametersByPathRequest request = new GetParametersByPathRequest()
                    .withPath(ssmPath)
                    .withRecursive(true)
                    .withNextToken(nextToken);

            result = ssmManager.getParametersByPath(request);
            for(Parameter p: result.getParameters())
            {

                String dp = (p.getName().substring(ssmPath.length()).split("/")[0]);

                uniquePartitions.add(dp);
            }
            nextToken = result.getNextToken();


        }
        while (nextToken != null);


        partitions.addAll(uniquePartitions);


        return partitions;
    }

    private String getTenantPrefix() {
        return awsServiceConfig.ssmPartitionPrefix;
    }
}
