/*
  Copyright 2002-2021 Google LLC
  Copyright 2002-2021 EPAM Systems, Inc

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.opengroup.osdu.partition.provider.reference.repository;

import static com.mongodb.client.model.Filters.eq;

import com.google.gson.Gson;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.DeleteResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.bson.Document;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.reference.model.PartitionPropertyDocument;
import org.opengroup.osdu.partition.provider.reference.persistence.MongoDdmsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PartitionPropertyEntityRepository {

  protected static final String PARTITIONS = "Partitions";
  protected static final String PARTITIONS_DATABASE = "PartitionsDB";
  protected static final String PARTITION_ID = "partitionId";
  private final MongoDdmsClient mongoDdmsClient;

  @Autowired
  public PartitionPropertyEntityRepository(MongoDdmsClient mongoDdmsClient) {
    this.mongoDdmsClient = mongoDdmsClient;
  }

  public Optional<PartitionInfo> createPartition(String partitionId, PartitionInfo partitionInfo) {
    if (Objects.nonNull(partitionInfo) && Objects.nonNull(partitionId)) {
      MongoCollection<Document> mongoCollection = mongoDdmsClient
          .getMongoCollection(PARTITIONS_DATABASE, PARTITIONS);
      PartitionPropertyDocument partitionPropertyDocument = convertToPartitionPropertyDocument(
          partitionId, partitionInfo);
      mongoCollection.replaceOne(eq(PARTITION_ID, partitionId),
          Document.parse(new Gson().toJson(partitionPropertyDocument)),
          new ReplaceOptions().upsert(true));
      return Optional.ofNullable(partitionInfo);
    }
    return Optional.empty();
  }

  public Optional<PartitionInfo> updatePartition(String partitionId, PartitionInfo partitionInfo) {
    if (Objects.nonNull(partitionInfo) && Objects.nonNull(partitionId)) {
      MongoCollection<Document> mongoCollection = mongoDdmsClient
          .getMongoCollection(PARTITIONS_DATABASE, PARTITIONS);
      Document result = mongoCollection.find(eq(PARTITION_ID, partitionId)).first();
      PartitionPropertyDocument partitionPropertyDocument = new Gson().fromJson(result.toJson(),
          PartitionPropertyDocument.class);
      partitionPropertyDocument.getProperties().putAll(partitionInfo.getProperties());

      mongoCollection.replaceOne(eq(PARTITION_ID, partitionId),
          Document.parse(new Gson().toJson(partitionPropertyDocument)),
          new ReplaceOptions().upsert(true));
      return Optional.ofNullable(partitionInfo);
    }
    return Optional.empty();
  }

  public Optional<PartitionInfo> findByPartitionId(String partitionId) {
    if (Objects.nonNull(partitionId)) {
      MongoCollection<Document> mongoCollection = mongoDdmsClient
          .getMongoCollection(PARTITIONS_DATABASE, PARTITIONS);
      Document result = mongoCollection.find(eq(PARTITION_ID, partitionId)).first();
      if (Objects.nonNull(result)) {
        PartitionPropertyDocument partitionPropertyDocument = new Gson().fromJson(result.toJson(),
            PartitionPropertyDocument.class);
        PartitionInfo partitionInfo = convertToPartitionInfo(partitionPropertyDocument);
        return Optional.ofNullable(partitionInfo);
      }
    }
    return Optional.empty();
  }

  public List<String> getAllPartitions() {
    MongoCollection<Document> mongoCollection = mongoDdmsClient
        .getMongoCollection(PARTITIONS_DATABASE, PARTITIONS);

    FindIterable<Document> results = mongoCollection.find();
    List<String> partitionsIds = new ArrayList<>();
    for (Document document : results) {
      PartitionPropertyDocument partitionPropertyDocument = new Gson().fromJson(document.toJson(),
          PartitionPropertyDocument.class);
      partitionsIds.add(partitionPropertyDocument.getPartitionId());
    }
    return partitionsIds;
  }

  public boolean isDeletedPartitionInfoByPartitionId(String partitionId) {
    if (Objects.nonNull(partitionId)) {
      MongoCollection<Document> mongoCollection = mongoDdmsClient
          .getMongoCollection(PARTITIONS_DATABASE, PARTITIONS);
      DeleteResult result = mongoCollection.deleteMany(eq(PARTITION_ID, partitionId));
      return result.wasAcknowledged();
    }
    return false;
  }

  public Optional<Property> findByPartitionIdAndName(String partitionId, String key) {
    if (Objects.nonNull(partitionId) && Objects.nonNull(key)) {
      MongoCollection<Document> mongoCollection = mongoDdmsClient
          .getMongoCollection(PARTITIONS_DATABASE, PARTITIONS);
      Document result = mongoCollection.find(eq(PARTITION_ID, partitionId)).first();
      if (Objects.nonNull(result)) {
        PartitionPropertyDocument partitionPropertyDocument = new Gson().fromJson(result.toJson(),
            PartitionPropertyDocument.class);
        Property property = partitionPropertyDocument.getProperties().get(key);
        if (Objects.nonNull(property)) {
          return Optional.ofNullable(property);
        }
      }
    }
    return Optional.empty();
  }

  private PartitionPropertyDocument convertToPartitionPropertyDocument(
      String partitionId, PartitionInfo partitionInfo) {
    PartitionPropertyDocument partitionPropertyDocument = new PartitionPropertyDocument();
    partitionPropertyDocument.setPartitionId(partitionId);
    partitionPropertyDocument.setProperties(partitionInfo.getProperties());
    return partitionPropertyDocument;
  }

  private PartitionInfo convertToPartitionInfo(
      PartitionPropertyDocument partitionPropertyDocument) {
    PartitionInfo partitionInfo = new PartitionInfo();
    partitionInfo.setProperties(partitionPropertyDocument.getProperties());
    return partitionInfo;
  }
}
