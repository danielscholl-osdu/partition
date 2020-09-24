/*
 * Copyright © 2020 Amazon Web Services
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

package org.opengroup.osdu.partition.util;

import com.google.common.base.Strings;
import org.opengroup.osdu.core.aws.cognito.AWSCognitoClient;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;

public class AwsTestUtils extends TestUtils {

    private AWSCognitoClient cognitoClient;

    public AwsTestUtils() {
        cognitoClient = new AWSCognitoClient();
    }

    @Override
    public synchronized String getAccessToken() throws Exception {
        if (Strings.isNullOrEmpty(token)) {
            token = cognitoClient.getTokenForUserWithAccess();
        }
        
        return "Bearer " + token;
    }

    @Override
    public synchronized String getNoAccessToken() throws Exception {
        if (Strings.isNullOrEmpty(noAccessToken)) {

            noAccessToken = createInvalidToken("baduser@example.com");
        }
        return "Bearer " + noAccessToken;
    }

    private static String createInvalidToken(String username) {

        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(2048);


    
            KeyPair kp = keyGenerator.genKeyPair();
            PublicKey publicKey = (PublicKey) kp.getPublic();
            PrivateKey privateKey = (PrivateKey) kp.getPrivate();
            
            
            String token = Jwts.builder()
                    .setSubject(username)
                    .setExpiration(new Date())                
                    .setIssuer("info@example.com")                    
                    // RS256 with privateKey
                    .signWith(SignatureAlgorithm.RS256, privateKey)
                    .compact();
                    
            return token;
        }
        catch (NoSuchAlgorithmException ex) {            
            return null;
        }
        

    }
}