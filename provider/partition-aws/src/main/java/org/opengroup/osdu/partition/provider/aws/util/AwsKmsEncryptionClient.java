// Copyright © 2021 Amazon Web Services
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

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.encryptionsdk.CommitmentPolicy;
import lombok.Data;

import org.opengroup.osdu.core.aws.iam.IAMConfig;
import org.opengroup.osdu.core.aws.ssm.K8sLocalParameterProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;


/**
 * <p>
 * Encrypts and then decrypts data using an AWS KMS key.
 * <p>
 */

@Data
@Component
public class AwsKmsEncryptionClient {

    @Value("${aws.kms.keyArn}")
    private String keyArn;

    @Value("${osdu.mongodb.database}")
    private String authDatabase;

    private AWSCredentialsProvider amazonAWSCredentials;

    private KmsMasterKeyProvider keyProvider;

    private static final AwsCrypto crypto = AwsCrypto.builder()
            .withCommitmentPolicy(CommitmentPolicy.RequireEncryptRequireDecrypt)
            .build();


    private final AwsCrypto instanceCrypto;

    public AwsKmsEncryptionClient() {
        instanceCrypto = crypto;
    }

    public AwsKmsEncryptionClient(final AwsCrypto argCrypto) {
        instanceCrypto = argCrypto;
    }

    @PostConstruct
    private void initializeKeyProvider() {

        // grab key arn from K8s
        K8sLocalParameterProvider provider = new K8sLocalParameterProvider();

        if (Boolean.FALSE.equals(provider.getLocalMode())) {
            keyArn = provider.getParameterAsStringOrDefault("KEY_ARN", keyArn);
        }

        // log in with IAM credentials
        amazonAWSCredentials = IAMConfig.amazonAWSCredentials();

        // generate keyProvider
        this.keyProvider = KmsMasterKeyProvider.builder()
                .withCredentials(amazonAWSCredentials)
                .buildStrict(keyArn);
    }

    public byte[] encrypt(String plainText, String id) {

        final Map<String, String> encryptionContext = generateEncryptionContext(id);
        final CryptoResult<byte[], KmsMasterKey> encryptResult = instanceCrypto.encryptData(keyProvider, plainText.getBytes(StandardCharsets.UTF_8), encryptionContext);
        return encryptResult.getResult();
    }

    public String decrypt(byte[] ciphertext, String id) {

        final CryptoResult<byte[], KmsMasterKey> decryptResult = instanceCrypto.decryptData(keyProvider, ciphertext);
        final Map<String, String> encryptionContext = generateEncryptionContext(id);

        // throw error if context doesn't match
        if (!encryptionContext.entrySet().stream()
                .allMatch(e -> e.getValue().equals(decryptResult.getEncryptionContext().get(e.getKey())))) {
            throw new IllegalStateException("Wrong Encryption Context!");
        }

        return new String(decryptResult.getResult(), StandardCharsets.UTF_8);
    }

    private Map<String, String> generateEncryptionContext(String id) {
        return Collections.singletonMap(authDatabase, id);
    }

}


