package org.opengroup.osdu.partition.api;

import org.junit.After;
import org.junit.Before;
import org.opengroup.osdu.partition.util.AwsTestUtils;

public class TestGetVersionInfo extends GetVersionInfoApiTest {

  @Before
  @Override
  public void setup() throws Exception {
    this.testUtils = new AwsTestUtils();
  }

  @After
  @Override
  public void tearDown() throws Exception {
    this.testUtils = null;
  }
}
