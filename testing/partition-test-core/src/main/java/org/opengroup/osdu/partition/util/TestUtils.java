/*
 * Copyright 2017-2020, Schlumberger
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

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public abstract class TestUtils {

	protected static String token = null;
	protected static String noAccessToken = null;

	public static String getApiPath(String api, boolean enforceHttp) throws Exception {
		String baseUrl = Config.Instance().hostUrl;
		if(enforceHttp)
			baseUrl = baseUrl.replaceFirst("https", "http");
		URL mergedURL = new URL(baseUrl + api);
		return mergedURL.toString();
	}

    public abstract String getAccessToken() throws Exception;

    public abstract String getNoAccessToken() throws Exception;

	public static ClientResponse send(String path, String httpMethod, String token, String requestBody, String query, boolean enforceHttp)
			throws Exception {

        Map<String, String> headers = getOsduTenantHeaders();

		return send(path, httpMethod, token, requestBody, query, headers, enforceHttp);
	}

    public static Map<String, String> getOsduTenantHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("data-partition-id", Config.Instance().osduTenant);
        return headers;
    }

	public static Map<String, String> getCustomerTenantHeaders() {
		Map<String, String> headers = new HashMap<>();
		headers.put("data-partition-id", Config.Instance().clientTenant);
		return headers;
	}

    public static ClientResponse send(String path, String httpMethod, String token, String requestBody, String query,
                               Map<String,String> headers, boolean enforceHttp)
            throws Exception {

        Client client = getClient();
		client.setConnectTimeout(1500000);
		client.setReadTimeout(1500000);
        client.setFollowRedirects(false);
        String url = getApiPath(path + query, enforceHttp);
        WebResource webResource = client.resource(url);
        final WebResource.Builder builder = webResource.type(MediaType.APPLICATION_JSON)
                .header("Authorization", token);
        headers.forEach((k, v) -> builder.header(k, v));
        ClientResponse response = builder.method(httpMethod, ClientResponse.class, requestBody);

        return response;
    }

	/** Referenced for Test cases where [Token, body] not required. ex [Swagger API] **/
	public static ClientResponse send(String path, String httpMethod, boolean enforceHttp) throws Exception {

		Client client = getClient();
		client.setConnectTimeout(1500000);
		client.setReadTimeout(1500000);
		String url = getApiPath(path, enforceHttp);
		WebResource webResource = client.resource(url);
		final WebResource.Builder builder = webResource.getRequestBuilder();
		ClientResponse response = builder.method(httpMethod, ClientResponse.class);
		return response;
	}

	@SuppressWarnings("unchecked")
	public <T> T getResult(ClientResponse response, int exepectedStatus, Class<T> classOfT) {
		String json = response.getEntity(String.class);

		assertEquals(exepectedStatus, response.getStatus());
		if (exepectedStatus == 204) {
			return null;
		}
		assertEquals(MediaType.APPLICATION_JSON, response.getType().toString());
		if (classOfT == String.class) {
			return (T) json;
		}
		Gson gson = new Gson();
		return gson.fromJson(json, classOfT);
	}

	public static Client getClient() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };

		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
		}
		allowMethods("PATCH");
		return Client.create();
	}

	private static void allowMethods(String... methods) {
		try {
			Field methodsField = HttpURLConnection.class.getDeclaredField("methods");

			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

			methodsField.setAccessible(true);

			String[] oldMethods = (String[]) methodsField.get(null);
			Set<String> methodsSet = new LinkedHashSet<>(Arrays.asList(oldMethods));
			methodsSet.addAll(Arrays.asList(methods));
			String[] newMethods = methodsSet.toArray(new String[0]);

			methodsField.set(null/*static field*/, newMethods);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
}
