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

package org.opengroup.osdu.partition.provider.aws.util;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.opengroup.osdu.core.aws.v2.iam.IAMConfig;
import org.opengroup.osdu.core.aws.v2.ssm.K8sLocalParameterProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptResponse;

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

	@Value("${aws.region}")
	private String awsRegion;

	private KmsClient kmsClient;

	public AwsKmsEncryptionClient() {
		// Empty constructor - KMS client will be initialized in the @PostConstruct method
	}

	public AwsKmsEncryptionClient(KmsClient kmsClient) {
		this.kmsClient = kmsClient;
	}

	@PostConstruct
	private void initialize() {
		// Initialize KMS client after properties are injected
		if (this.kmsClient == null) {
			this.kmsClient = KmsClient.builder()
				.region(Region.of(this.awsRegion))
				.credentialsProvider(IAMConfig.iamCredentialsProvider())
				.build();
		}

		// grab key arn from K8s
		K8sLocalParameterProvider provider = new K8sLocalParameterProvider();
		if (Boolean.FALSE.equals(provider.getLocalMode())) {
			keyArn = provider.getParameterAsStringOrDefault("KEY_ARN", keyArn);
		}
	}

	public byte[] encrypt(String plainText, String id) {
		Map<String, String> encryptionContext = generateEncryptionContext(id);

		EncryptRequest request = EncryptRequest.builder().keyId(keyArn)
				.plaintext(SdkBytes.fromString(plainText, StandardCharsets.UTF_8)).encryptionContext(encryptionContext)
				.build();

		EncryptResponse response = kmsClient.encrypt(request);
		return response.ciphertextBlob().asByteArray();
	}

	public String decrypt(byte[] ciphertext, String id) {
		Map<String, String> encryptionContext = generateEncryptionContext(id);

		DecryptRequest request = DecryptRequest.builder().keyId(keyArn)
				.ciphertextBlob(SdkBytes.fromByteArray(ciphertext)).encryptionContext(encryptionContext).build();

		DecryptResponse response = kmsClient.decrypt(request);
		return response.plaintext().asString(StandardCharsets.UTF_8);
	}

	private Map<String, String> generateEncryptionContext(String id) {
		return Collections.singletonMap(authDatabase, id);
	}
}
