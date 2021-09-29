/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.partition.api;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.util.IBMTestUtils;

import com.sun.jersey.api.client.ClientResponse;

public class TestGetPartitionById extends GetPartitionByIdApitTest {

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

}
