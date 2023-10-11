// Copyright © 2020 Amazon Web Services
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

package org.opengroup.osdu.partition.provider.aws.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.Assert.fail;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoResult;
import com.amazonaws.encryptionsdk.kms.KmsMasterKey;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.aws.ssm.K8sLocalParameterProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class AwsKmsEncryptionClientTest {


    @Mock
    private KmsMasterKeyProvider.Builder kmsBuilder;

    @Mock
    private KmsMasterKeyProvider keyProvider;

    @Mock
    private AwsCrypto instanceCrypto;

    @Mock
    private CryptoResult<byte[], KmsMasterKey> cryptoResult;

    @Captor
    private ArgumentCaptor<Map<String, String>> encryptionContext;

    @Captor
    private ArgumentCaptor<byte[]> bytesCaptor;

    @InjectMocks
    private AwsKmsEncryptionClient encryptionClient;

    private final String KEY_ARN = "key_arn";

    private final byte[] ENCRYPTED = HexFormat.of().parseHex("0123456789abcdef");

    private byte[] DECRYPTED_BYTES;

    private final String DECRYPTED = "Decrypted";
    private final String AUTH_DATABASE = "osdu";
    private final String VALID_ID = "valid_id";

    private final Map<String, String> validContext = new HashMap<>();

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        ReflectionTestUtils.setField(encryptionClient, "keyArn", KEY_ARN);
        ReflectionTestUtils.setField(encryptionClient, "authDatabase", AUTH_DATABASE);
        ReflectionTestUtils.setField(encryptionClient, "keyProvider", keyProvider);

        DECRYPTED_BYTES = DECRYPTED.getBytes(StandardCharsets.UTF_8);
        validContext.put(AUTH_DATABASE, VALID_ID);
    }

    @Test
    public void should_return_when_initCalledWithLocal() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final String LOCAL_KEY_ARN = "local_key_arn";
        Method initializeKeyProvider = AwsKmsEncryptionClient.class.getDeclaredMethod("initializeKeyProvider");
        initializeKeyProvider.setAccessible(true);
        ReflectionTestUtils.setField(encryptionClient, "keyArn", LOCAL_KEY_ARN);
        try (MockedConstruction<K8sLocalParameterProvider> k8sParameterProvider = Mockito.mockConstruction(K8sLocalParameterProvider.class,
                                                                                                           (mock, context) -> {
                                                                                                               when(mock.getParameterAsStringOrDefault(anyString(), anyString())).thenReturn("bogus_key_arn");
                                                                                                               when(mock.getLocalMode()).thenReturn(true);
                                                                                                           })) {
            try (MockedStatic<KmsMasterKeyProvider> kmsMKPStatic = Mockito.mockStatic(KmsMasterKeyProvider.class)) {
                kmsMKPStatic.when(KmsMasterKeyProvider::builder).thenReturn(kmsBuilder);
                when(kmsBuilder.withCredentials(any(AWSCredentialsProvider.class))).thenReturn(kmsBuilder);
                when(kmsBuilder.buildStrict(anyString())).thenReturn(keyProvider);

                initializeKeyProvider.invoke(encryptionClient);

                assertTrue("Must pass when `initializeKeyProvider` called with local mode enabled!", true);
                assertEquals(LOCAL_KEY_ARN, ReflectionTestUtils.getField(encryptionClient, "keyArn"));
            }
        } finally {
            ReflectionTestUtils.setField(encryptionClient, "keyArn", KEY_ARN);
        }
    }
    @Test
    public void should_return_when_initCalledWithoutLocal() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final String OSDU_KEY_ARN = "osdu_key_arn";
        Method initializeKeyProvider = AwsKmsEncryptionClient.class.getDeclaredMethod("initializeKeyProvider");
        initializeKeyProvider.setAccessible(true);
        ReflectionTestUtils.setField(encryptionClient, "keyArn", "bogus_key_arn");
        try (MockedConstruction<K8sLocalParameterProvider> k8sParameterProvider = Mockito.mockConstruction(K8sLocalParameterProvider.class,
                                                                                                           (mock, context) -> {
                                                                                                               when(mock.getParameterAsStringOrDefault(anyString(), anyString())).thenReturn(OSDU_KEY_ARN);
                                                                                                               when(mock.getLocalMode()).thenReturn(false);
                                                                                                           })) {
            try (MockedStatic<KmsMasterKeyProvider> kmsMKPStatic = Mockito.mockStatic(KmsMasterKeyProvider.class)) {
                kmsMKPStatic.when(KmsMasterKeyProvider::builder).thenReturn(kmsBuilder);
                when(kmsBuilder.withCredentials(any(AWSCredentialsProvider.class))).thenReturn(kmsBuilder);
                when(kmsBuilder.buildStrict(anyString())).thenReturn(keyProvider);

                initializeKeyProvider.invoke(encryptionClient);

                assertTrue("Must pass when `initializeKeyProvider` called with OSDU mode enabled!", true);
                assertEquals(OSDU_KEY_ARN, ReflectionTestUtils.getField(encryptionClient, "keyArn"));
            }
        } finally {
            ReflectionTestUtils.setField(encryptionClient, "keyArn", KEY_ARN);
        }
    }

    @Test
    public void should_return_when_encryptCalledWithArgs() {
        when(instanceCrypto.encryptData(any(KmsMasterKeyProvider.class), bytesCaptor.capture(), encryptionContext.capture())).thenReturn(cryptoResult);
        when(cryptoResult.getResult()).thenReturn(ENCRYPTED);

        byte[] encrypted = encryptionClient.encrypt(DECRYPTED, VALID_ID);

        assertArrayEquals(ENCRYPTED, encrypted);
        assertArrayEquals(DECRYPTED_BYTES, bytesCaptor.getValue());
        Map<String, String> map = encryptionContext.getValue();
        assertEquals(1, map.size());
        assertTrue(map.containsKey(AUTH_DATABASE));
        assertEquals(VALID_ID, map.get(AUTH_DATABASE));
    }

    @Test
    public void should_return_when_decryptCalledWithValidArgs() {
        when(instanceCrypto.decryptData(any(KmsMasterKeyProvider.class), bytesCaptor.capture())).thenReturn(cryptoResult);
        when(cryptoResult.getResult()).thenReturn(DECRYPTED_BYTES);
        when(cryptoResult.getEncryptionContext()).thenReturn(validContext);

        String decrypted = encryptionClient.decrypt(ENCRYPTED, VALID_ID);

        assertEquals(DECRYPTED, decrypted);
        assertArrayEquals(ENCRYPTED, bytesCaptor.getValue());
    }

    @Test
    public void should_ThrowIllegalStateException_when_decryptCalledWithInValidArgs() {
        when(instanceCrypto.decryptData(any(KmsMasterKeyProvider.class), bytesCaptor.capture())).thenReturn(cryptoResult);
        when(cryptoResult.getEncryptionContext()).thenReturn(validContext);

        try {
            String INVALID_ID = "invalid_id";
            encryptionClient.decrypt(ENCRYPTED, INVALID_ID);
            fail("Should throw exception when encryption contexts do not match.");
        } catch (IllegalStateException exception) {
            assertEquals("Wrong Encryption Context!", exception.getMessage());
        }
        assertArrayEquals(ENCRYPTED, bytesCaptor.getValue());
    }

    @Test
    public void should_return_when_defaultConstructorCalled() {
        AwsCrypto expectedCrypto = (AwsCrypto) ReflectionTestUtils.getField(AwsKmsEncryptionClient.class, "crypto");

        AwsKmsEncryptionClient cryptoClient = new AwsKmsEncryptionClient();

        AwsCrypto actualCrypto = (AwsCrypto) ReflectionTestUtils.getField(cryptoClient, "crypto");
        assertEquals(expectedCrypto, actualCrypto);
    }
}
