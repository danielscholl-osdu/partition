/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.partition.api;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.util.IBMTestUtils;

import com.sun.jersey.api.client.ClientResponse;

public class TestDeletePartition extends DeletePartitionTest {
    
    @Before
    @Override
    public void setup() {
        this.testUtils = new IBMTestUtils();
    }

    @After
    @Override
    public void tearDown() {
        this.testUtils = null;
    }

    @Test
	@Override
	public void should_return401_when_noAccessToken() throws Exception {
		// Springboot Keycloak gives 403 when token does not have required roles
    	 ClientResponse response = descriptor.runOnCustomerTenant(getId(), testUtils.getNoAccessToken());
         assertEquals(error(response.getEntity(String.class)), 403, response.getStatus());
	}

    @Test
	@Override
	public void should_return401_when_accessingWithCredentialsWithoutPermission() throws Exception {
		// Partition-ibm service does not required partition id
    	// Here, no access token used hence checking with 403 response code in assertion statement  
    	ClientResponse response = descriptor.run(getId(), testUtils.getNoAccessToken());
        assertEquals(403, response.getStatus());
	}
}
