// Copyright 2017-2020, Schlumberger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.partition.api;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengroup.osdu.partition.util.AzureTestUtils;

public class TestCreatePartition extends CreatePartitionTest {

    @Before
    @Override
    public void setup() {
        this.testUtils = new AzureTestUtils();
    }

    @After
    @Override
    public void tearDown() {
        this.testUtils = null;
    }

    @Test
    @Override
    public void should_return401_when_noAccessToken() throws Exception {
        // revisit this later -- Istio is changing the response code
    }

    @Test
    @Override
    public void should_return401_when_accessingWithCredentialsWithoutPermission() throws Exception {
        // revisit this later -- Istio is changing the response code
    }

    @Test
    @Override
    public void should_return401_when_makingHttpRequestWithoutToken() throws Exception {
        // revisit this later -- Istio is changing the response code
    }
}
