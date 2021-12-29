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

package org.opengroup.osdu.partition.provider.gcp.osm.repository;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomUtils;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.gcp.osm.model.Destination;
import org.opengroup.osdu.core.gcp.osm.model.query.GetQuery;
import org.opengroup.osdu.core.gcp.osm.model.where.Where;
import org.opengroup.osdu.core.gcp.osm.service.Context;
import org.opengroup.osdu.core.gcp.osm.service.Transaction;
import org.opengroup.osdu.core.gcp.osm.translate.TranslatorException;
import org.opengroup.osdu.partition.provider.gcp.model.PartitionPropertyEntity;
import org.opengroup.osdu.partition.provider.gcp.osm.config.provider.OsmPartitionDestinationProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.opengroup.osdu.core.gcp.osm.model.where.condition.And.and;
import static org.opengroup.osdu.core.gcp.osm.model.where.predicate.Eq.eq;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty("osmDriver")
public class OsmPartitionPropertyRepository {

    public static final String PARTITION_ID_FILED = "partition_id";
    private static final String NAME_FILED = "name";
    private final OsmPartitionDestinationProvider osmPartitionDestinationProvider;
    private final Context context;


    public Optional<List<PartitionPropertyEntity>> findByPartitionId(String partitionId) {
        List<PartitionPropertyEntity> properties =
                context.getResultsAsList(buildPartitionEntityQueryBy(eq(PARTITION_ID_FILED, partitionId)));
        return (CollectionUtils.isEmpty(properties)) ?
                Optional.empty():
                Optional.of(properties);
    }

    public PartitionPropertyEntity findByPartitionIdAndName(String partitionId, String key) {
        List<PartitionPropertyEntity> propertyEntities = context
                .getResultsAsList(buildPartitionEntityQueryBy(and(
                            eq(PARTITION_ID_FILED, partitionId),
                            eq(NAME_FILED, key))
                        )
                );
        return propertyEntities.stream().findFirst().orElse(null);
    }

    public List<String> getAllPartitions() {
        return context.getResultsAsList(
                    new GetQuery<>(PartitionPropertyEntity.class, osmPartitionDestinationProvider.getDestination()))
                .stream()
                .map(PartitionPropertyEntity::getPartitionId)
                .distinct()
                .collect(Collectors.toList());
    }

    public void deleteByPartitionId(String partitionId) {
        Destination destination = osmPartitionDestinationProvider.getDestination();
        Transaction tx = null;
        try{

            tx = context.beginTransaction(destination);

            context.delete(
                    PartitionPropertyEntity.class,
                    osmPartitionDestinationProvider.getDestination(),
                    eq(PARTITION_ID_FILED, partitionId));

            tx.commitIfActive();
        } catch (TranslatorException e) {
            log.error("Error during partition delete", e);
            throw new AppException(
                    INTERNAL_SERVER_ERROR.value(),
                    INTERNAL_SERVER_ERROR.getReasonPhrase(),
                    "Error during partition delete");
        } finally {
            if (ObjectUtils.isNotEmpty(tx)){
                tx.rollbackIfActive();
            }
        }
    }

    public void saveAll(List<PartitionPropertyEntity> partitionProperties) {
        Destination destination = osmPartitionDestinationProvider.getDestination();
        Transaction tx = null;
        try{
            tx = context.beginTransaction(destination);
            for (PartitionPropertyEntity entity : partitionProperties){
                if (ObjectUtils.isEmpty(entity.getId())) {
                    generateAvailableIdFor(entity);
                }
                context.upsert(entity, destination);
            }
            tx.commitIfActive();
        }
        finally {
            if (ObjectUtils.isNotEmpty(tx)){
                tx.rollbackIfActive();
            }
        }
    }

    private void generateAvailableIdFor(PartitionPropertyEntity entity) {
        //TODO Should be removed when GONRG-4077 will be ready
        Long id = RandomUtils.nextLong();
        if (ObjectUtils.isNotEmpty(context.getOne(buildPartitionEntityQueryBy(eq("id", id))))){
            generateAvailableIdFor(entity);
        } else {
            entity.setId(id.toString());
        }
    }

    private GetQuery<PartitionPropertyEntity> buildPartitionEntityQueryBy(Where where){
        return new GetQuery<>(PartitionPropertyEntity.class, osmPartitionDestinationProvider.getDestination(), where);
    }
}
