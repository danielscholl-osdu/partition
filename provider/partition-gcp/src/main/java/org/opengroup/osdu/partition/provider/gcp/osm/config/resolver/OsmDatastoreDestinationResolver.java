/*
 * Copyright 2020-2021 Google LLC
 * Copyright 2020-2021 EPAM Systems, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengroup.osdu.partition.provider.gcp.osm.config.resolver;

import com.google.api.gax.retrying.RetrySettings;
import com.google.cloud.TransportOptions;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.http.HttpTransportOptions;
import lombok.RequiredArgsConstructor;
import org.opengroup.osdu.core.gcp.osm.model.Destination;
import org.opengroup.osdu.core.gcp.osm.translate.datastore.DsDestinationResolution;
import org.opengroup.osdu.core.gcp.osm.translate.datastore.DsDestinationResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.threeten.bp.Duration;

import java.io.IOException;
import java.util.Map;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

@Component
@Scope(SCOPE_SINGLETON)
@ConditionalOnProperty(name = "osmDriver", havingValue = "datastore")
@RequiredArgsConstructor
public class OsmDatastoreDestinationResolver implements DsDestinationResolver {

    protected static final RetrySettings RETRY_SETTINGS = RetrySettings.newBuilder().setMaxAttempts(6).setInitialRetryDelay(Duration.ofSeconds(10L)).setMaxRetryDelay(Duration.ofSeconds(32L)).setRetryDelayMultiplier(2.0D).setTotalTimeout(Duration.ofSeconds(50L)).setInitialRpcTimeout(Duration.ofSeconds(50L)).setRpcTimeoutMultiplier(1.0D).setMaxRpcTimeout(Duration.ofSeconds(50L)).build();
    protected static final TransportOptions TRANSPORT_OPTIONS = HttpTransportOptions.newBuilder().setReadTimeout(30000).build();
    private final Map<String, Datastore> datastoreCache;

    /**
     * Takes provided Destination with partitionId set to needed tenantId, returns its TenantInfo.projectId.
     *
     * @param destination to resolve
     * @return resolution results
     */
    @Override
    public DsDestinationResolution resolve(Destination destination) {
        String projectId = destination.getPartitionId();
        Datastore datastore = datastoreCache.computeIfAbsent(projectId, key -> getDatastoreFor(destination, key, projectId));

        return DsDestinationResolution.builder()
                .projectId(datastore.getOptions().getProjectId())
                .datastore(datastore)
                .build();
    }

    private Datastore getDatastoreFor(Destination destination, String key, String projectId) {
        return datastoreCache.computeIfAbsent(key, k ->
                DatastoreOptions.newBuilder()
                    .setRetrySettings(RETRY_SETTINGS)
                    .setTransportOptions(TRANSPORT_OPTIONS)
                    .setProjectId(projectId)
                    .setNamespace(destination.getNamespace().getName())
                    .build()
                    .getService());
    }

    @Override
    public void close() throws IOException {
        //Method stub
    }
}