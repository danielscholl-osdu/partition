/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.partition.provider.ibm.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.ibm.auth.ServiceCredentials;
import org.opengroup.osdu.core.ibm.cloudant.IBMCloudantClientFactory;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.opengroup.osdu.partition.provider.ibm.model.PartitionDoc;
import org.opengroup.osdu.partition.provider.interfaces.IPartitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;
import com.cloudant.client.org.lightcouch.DocumentConflictException;
import com.cloudant.client.org.lightcouch.NoDocumentException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PartitionServiceImpl implements IPartitionService {

	private static final String PARTITION_DATABASE = "partition";

	@Autowired
	private JaxRsDpsLog logger;

	Database db;

	private IBMCloudantClientFactory cloudantFactory;

	@Value("${ibm.db.url}")
	private String dbUrl;
	@Value("${ibm.db.apikey:#{null}}")
	private String apiKey;
	@Value("${ibm.db.user:#{null}}")
	private String dbUser;
	@Value("${ibm.db.password:#{null}}")
	private String dbPassword;
	@Value("${ibm.env.prefix:local-dev}")
	private String dbNamePrefix;

	public PartitionServiceImpl() {

	}

	@PostConstruct
	public void init()  {
		cloudantFactory = new IBMCloudantClientFactory(new ServiceCredentials(dbUrl, dbUser, dbPassword));
		try {
			db = cloudantFactory.getDatabase(dbNamePrefix, PARTITION_DATABASE);
		} catch (MalformedURLException e) {
			log.error("malformed URL has occurred.", e);
			e.printStackTrace();
		}
	}

	@Override
	public PartitionInfo createPartition(String partitionId, PartitionInfo partitionInfo) {
		PartitionDoc partitionDoc = new PartitionDoc(partitionId, partitionInfo);
		try {
			db.save(partitionDoc);
			return partitionInfo;
		} catch (DocumentConflictException e) {
			log.error("Partition already exists");
			throw new AppException(e.getStatusCode(), "Conflict", "partition already exists", e);
		} catch (Exception e) {
			log.info("Partition creation failed ");
			e.printStackTrace();
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage(), "Partition creation failed", e);
		}
		
	}

	@Override
	public PartitionInfo updatePartition(String partitionId, PartitionInfo partitionInfo) {
		if (partitionInfo.getProperties().containsKey("id")) {
			throw new AppException(HttpStatus.SC_BAD_REQUEST, "can not update id", "the field id can not be updated");
		}
		try {
			PartitionDoc partitionDoc = db.find(PartitionDoc.class, partitionId);
			partitionDoc.getPartitionInfo().getProperties().putAll(partitionInfo.getProperties());
			Response update = db.update(partitionDoc);
			return partitionDoc.getPartitionInfo();
		} catch (NoDocumentException e) {
			log.error(String.format("%s partition does not exists", partitionId));
			e.printStackTrace();
			throw new AppException(e.getStatusCode(), "Partition not found",
					String.format("%s Update failed. Create partition first. partition does not exists", partitionId),
					e);
		} catch (DocumentConflictException e) {
			log.error("Partition update failed. conflict is detected during the update");
			e.printStackTrace();
			throw new AppException(e.getStatusCode(), e.getReason(), e.getMessage(), e);
		} catch (Exception e) {
			log.error("Partition update failed");
			e.printStackTrace();
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Partition update failed", e.getMessage(), e);
		}

	}

	@Override
	public PartitionInfo getPartition(String partitionId) {
		PartitionDoc partitionDoc = null;
		try {
			partitionDoc = db.find(PartitionDoc.class, partitionId);
		} catch (NoDocumentException e) {
			log.error(String.format("%s partition does not exists", partitionId));
			e.printStackTrace();
			throw new AppException(e.getStatusCode(), e.getReason(), String.format("%s partition does not exists", partitionId), e);
		} catch (Exception e) {
			log.error("Partition could not found");
			e.printStackTrace();
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "unknown error", "Partition could not found", e );
		}
		return partitionDoc.getPartitionInfo();
	}

	@Override
	public boolean deletePartition(String partitionId) {
		Response deleteStatus = null;
		try {
			PartitionDoc partitionDoc = db.find(PartitionDoc.class, partitionId);
			deleteStatus = db.remove(partitionDoc);
		} catch (NoDocumentException e) {
			log.error(String.format("Deletion failed. Could not find partition ", partitionId));
			e.printStackTrace();
			throw new AppException(e.getStatusCode(), e.getReason(), String.format("Deletion failed. Could not find partition %s", partitionId), e);
		} catch (Exception e) {
			log.error("Deletion Failed. Unexpected error");
			e.printStackTrace();
		}
		if(deleteStatus.getStatusCode() == 200) {
			return true;
		} 
		return false;
		
	}

	@Override
	public List<String> getAllPartitions() {
		List<String> partitionList = null;
		try {
			partitionList = db.getAllDocsRequestBuilder().includeDocs(true).build().getResponse().getDocIds();
		} catch (IOException e) {
			log.error("Partitions could not found. IOException occurred", e);
			e.printStackTrace();
		} catch (Exception e) {
			log.error("Partition could not found.", e);
			e.printStackTrace();
		}
		return partitionList;
	}

}
