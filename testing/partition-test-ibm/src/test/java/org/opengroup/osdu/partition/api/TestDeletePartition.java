/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.partition.api;

import org.junit.After;
import org.junit.Before;
import org.opengroup.osdu.partition.util.IBMTestUtils;

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

}
