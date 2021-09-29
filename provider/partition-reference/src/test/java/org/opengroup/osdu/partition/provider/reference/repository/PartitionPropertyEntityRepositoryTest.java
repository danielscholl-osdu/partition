package org.opengroup.osdu.partition.provider.reference.repository;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.opengroup.osdu.partition.provider.reference.repository.PartitionPropertyEntityRepository.PARTITIONS;
import static org.opengroup.osdu.partition.provider.reference.repository.PartitionPropertyEntityRepository.PARTITIONS_DATABASE;
import static org.opengroup.osdu.partition.provider.reference.repository.PartitionPropertyEntityRepository.PARTITION_ID;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bson.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.model.Property;
import org.opengroup.osdu.partition.provider.reference.config.MongoDBConfigProperties;
import org.opengroup.osdu.partition.provider.reference.persistence.MongoDdmsClient;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoOperations;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({MongoOperations.class, MongoClient.class})
public class PartitionPropertyEntityRepositoryTest {

  private static final String PARTITION_ID_VALUE = "PartitionId";

  @Mock
  private MongoDdmsClient mongoDdmsClient;

  @MockBean
  private MongoOperations ops;

  @MockBean
  private MongoDBConfigProperties mongoDBConfigProperties;

  @InjectMocks
  private PartitionPropertyEntityRepository repo;

  @Test
  public void testCreatePartition() {
    when(mongoDdmsClient.getMongoCollection(PARTITIONS_DATABASE, PARTITIONS)).thenReturn(
        mock(MongoCollection.class));

    PartitionInfo expectedResult = buildPartitionInfo();
    Optional<PartitionInfo> actualResult = repo.createPartition(PARTITION_ID_VALUE, expectedResult);

    assertEquals(expectedResult, actualResult.get());
  }

  @Test
  public void testCreatePartitionWithNullPartitionId() {
    PartitionInfo expectedResult = buildPartitionInfo();
    Optional<PartitionInfo> partition = repo.createPartition(null, expectedResult);

    assertFalse(partition.isPresent());
  }

  @Test
  public void testUpdatePartition() {
    MongoCollection<Document> mongoCollection = mock(MongoCollection.class);
    Document result = mock(Document.class);
    when(mongoDdmsClient.getMongoCollection(PARTITIONS_DATABASE, PARTITIONS)).thenReturn(
        mongoCollection);
    when(mongoCollection.find(eq(PARTITION_ID, PARTITION_ID_VALUE))).thenReturn(
        mock(FindIterable.class));
    when(mongoCollection.find(eq(PARTITION_ID, PARTITION_ID_VALUE)).first()).thenReturn(result);
    when(result.toJson()).thenReturn(
        "{\"_id\": {\"$oid\": \"ID\"}, \"partitionId\": \"PartitionId\", \"properties\": {\"bucket\": {\"sensitive\": false, \"value\": \"osdu-cicd-epam-records\"}, \"crmAccountID\": {\"sensitive\": false, \"value\": \"[\\\"cicd\\\",\\\"opendes1234\\\"]\"}, \"policy-service-enabled\": {\"sensitive\": false, \"value\": \"false\"}, \"name\": {\"sensitive\": false, \"value\": \"opendes1234\"}, \"compliance-ruleset\": {\"sensitive\": false, \"value\": \"shared\"}, \"projectId\": {\"sensitive\": false, \"value\": \"osdu\"}}}");

    PartitionInfo expectedResult = buildPartitionInfo();
    Optional<PartitionInfo> actualResult = repo.updatePartition(PARTITION_ID_VALUE,
        expectedResult);

    assertEquals(expectedResult, actualResult.get());
  }

  @Test
  public void testUpdatePartitionWithNullPartitionInfo() {
    Optional<PartitionInfo> actualResult = repo.updatePartition(PARTITION_ID_VALUE, null);

    assertFalse(actualResult.isPresent());
  }

  @Test
  public void testDeletePartition() {
    MongoCollection<Document> mongoCollection = mock(MongoCollection.class);
    DeleteResult deleteResult = mock(DeleteResult.class);
    when(mongoDdmsClient.getMongoCollection(PARTITIONS_DATABASE, PARTITIONS)).thenReturn(
        mongoCollection);
    when(mongoCollection.deleteMany(eq(PARTITION_ID, PARTITION_ID_VALUE))).thenReturn(deleteResult);
    when(deleteResult.wasAcknowledged()).thenReturn(true);

    boolean result = repo.isDeletedPartitionInfoByPartitionId(PARTITION_ID_VALUE);

    assertTrue(result);
  }

  @Test
  public void testDeletePartitionWithNullPartitionId() {
    boolean result = repo.isDeletedPartitionInfoByPartitionId(null);

    assertFalse(result);
  }

  @Test
  public void testFindByPartitionId() {
    MongoCollection<Document> mongoCollection = mock(MongoCollection.class);
    Document result = mock(Document.class);
    when(mongoDdmsClient.getMongoCollection(PARTITIONS_DATABASE, PARTITIONS)).thenReturn(
        mongoCollection);
    when(mongoCollection.find(eq(PARTITION_ID, PARTITION_ID_VALUE))).thenReturn(
        mock(FindIterable.class));
    when(mongoCollection.find(eq(PARTITION_ID, PARTITION_ID_VALUE)).first()).thenReturn(result);
    when(result.toJson()).thenReturn(
        "{\"_id\": {\"$oid\": \"ID\"}, \"partitionId\": \"PartitionId\", \"properties\": {\"bucket\": {\"sensitive\": false, \"value\": \"osdu-cicd-epam-records\"}, \"crmAccountID\": {\"sensitive\": false, \"value\": \"[\\\"cicd\\\",\\\"opendes1234\\\"]\"}, \"policy-service-enabled\": {\"sensitive\": false, \"value\": \"false\"}, \"name\": {\"sensitive\": false, \"value\": \"opendes1234\"}, \"compliance-ruleset\": {\"sensitive\": false, \"value\": \"shared\"}, \"projectId\": {\"sensitive\": false, \"value\": \"osdu\"}}}");

    Optional<PartitionInfo> actualResult = repo.findByPartitionId(PARTITION_ID_VALUE);

    assertNotNull(actualResult.get());
  }

  @Test
  public void testFindByPartitionIdWithoutFoundDocument() {
    MongoCollection<Document> mongoCollection = mock(MongoCollection.class);
    when(mongoDdmsClient.getMongoCollection(PARTITIONS_DATABASE, PARTITIONS)).thenReturn(
        mongoCollection);
    when(mongoCollection.find(eq(PARTITION_ID, PARTITION_ID_VALUE))).thenReturn(
        mock(FindIterable.class));
    when(mongoCollection.find(eq(PARTITION_ID, PARTITION_ID_VALUE)).first()).thenReturn(null);

    Optional<PartitionInfo> actualResult = repo.findByPartitionId(PARTITION_ID_VALUE);

    assertFalse(actualResult.isPresent());
  }

  @Test
  public void testFindByPartitionIdWithNullPartitionId() {
    Optional<PartitionInfo> actualResult = repo.findByPartitionId(null);

    assertFalse(actualResult.isPresent());
  }

  @Test
  public void testFindByPartitionIdAndName() {
    MongoCollection<Document> mongoCollection = mock(MongoCollection.class);
    Document result = mock(Document.class);
    when(mongoDdmsClient.getMongoCollection(PARTITIONS_DATABASE, PARTITIONS)).thenReturn(
        mongoCollection);
    when(mongoCollection.find(eq(PARTITION_ID, PARTITION_ID_VALUE))).thenReturn(
        mock(FindIterable.class));
    when(mongoCollection.find(eq(PARTITION_ID, PARTITION_ID_VALUE)).first()).thenReturn(result);
    when(result.toJson()).thenReturn(
        "{\"_id\": {\"$oid\": \"ID\"}, \"partitionId\": \"PartitionId\", \"properties\": {\"bucket\": {\"sensitive\": false, \"value\": \"osdu-cicd-epam-records\"}, \"crmAccountID\": {\"sensitive\": false, \"value\": \"[\\\"cicd\\\",\\\"opendes1234\\\"]\"}, \"policy-service-enabled\": {\"sensitive\": false, \"value\": \"false\"}, \"name\": {\"sensitive\": false, \"value\": \"opendes1234\"}, \"compliance-ruleset\": {\"sensitive\": false, \"value\": \"shared\"}, \"projectId\": {\"sensitive\": false, \"value\": \"osdu\"}}}");

    Optional<Property> actualResult = repo.findByPartitionIdAndName(PARTITION_ID_VALUE, "bucket");

    assertNotNull(actualResult.get());

  }

  @Test
  public void testFindByPartitionIdAndNameWithNullPartitionId() {
    Optional<Property> actualResult = repo.findByPartitionIdAndName(null, "bucket");
    assertFalse(actualResult.isPresent());
  }

  @Test
  public void testFindByPartitionIdAndNameWithoutFoundDocument() {
    MongoCollection<Document> mongoCollection = mock(MongoCollection.class);
    when(mongoDdmsClient.getMongoCollection(PARTITIONS_DATABASE, PARTITIONS)).thenReturn(
        mongoCollection);
    when(mongoCollection.find(eq(PARTITION_ID, PARTITION_ID_VALUE))).thenReturn(
        mock(FindIterable.class));
    when(mongoCollection.find(eq(PARTITION_ID, PARTITION_ID_VALUE)).first()).thenReturn(null);

    Optional<Property> actualResult = repo.findByPartitionIdAndName(PARTITION_ID_VALUE, "nonExist");

    assertFalse(actualResult.isPresent());
  }

  @Test
  public void testFindByPartitionIdAndNameWithoutFoundProperty() {
    MongoCollection<Document> mongoCollection = mock(MongoCollection.class);
    Document result = mock(Document.class);
    when(mongoDdmsClient.getMongoCollection(PARTITIONS_DATABASE, PARTITIONS)).thenReturn(
        mongoCollection);
    when(mongoCollection.find(eq(PARTITION_ID, PARTITION_ID_VALUE))).thenReturn(
        mock(FindIterable.class));
    when(mongoCollection.find(eq(PARTITION_ID, PARTITION_ID_VALUE)).first()).thenReturn(result);
    when(result.toJson()).thenReturn(
        "{\"_id\": {\"$oid\": \"ID\"}, \"partitionId\": \"PartitionId\", \"properties\": {\"bucket\": {\"sensitive\": false, \"value\": \"osdu-cicd-epam-records\"}, \"crmAccountID\": {\"sensitive\": false, \"value\": \"[\\\"cicd\\\",\\\"opendes1234\\\"]\"}, \"policy-service-enabled\": {\"sensitive\": false, \"value\": \"false\"}, \"name\": {\"sensitive\": false, \"value\": \"opendes1234\"}, \"compliance-ruleset\": {\"sensitive\": false, \"value\": \"shared\"}, \"projectId\": {\"sensitive\": false, \"value\": \"osdu\"}}}");

    Optional<Property> actualResult = repo.findByPartitionIdAndName(PARTITION_ID_VALUE, "nonExist");

    assertFalse(actualResult.isPresent());
  }


  private PartitionInfo buildPartitionInfo() {
    PartitionInfo partitionInfo = new PartitionInfo();
    Map<String, Property> properties = new HashMap<>();
    properties.put("compliance-ruleset", new Property(true, "value"));
    properties.put("dataPartitionId", new Property(false, "PartitionId"));
    partitionInfo.setProperties(properties);

    return partitionInfo;
  }
}
