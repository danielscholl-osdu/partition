// Copyright © 2021 Amazon Web Services
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

package org.opengroup.osdu.partition.provider.aws.util;

import com.mongodb.ConnectionString;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;

import javax.inject.Inject;

@Configuration
public class MongoConfig {

    @Inject
    MongoProperties props;

    public String getMongoURI() {

        Boolean useSrvEndpoint = Boolean.parseBoolean(props.getUseSrvEndpointStr());

        if (useSrvEndpoint) {

            String srvUriFormat = "mongodb+srv://%s:%s@%s/%s?ssl=%s&retryWrites=%s&w=%s";

            String srvUri = String.format(
                    srvUriFormat,
                    props.getUsername(),
                    props.getPassword(),
                    props.getEndpoint(),
                    props.getAuthDatabase(),
                    props.getEnableTLS(),
                    props.getRetryWrites(),
                    props.getWriteMode());

            return srvUri;
        }
        else {
            String uriFormat = "mongodb+srv://%s:%s@%s/%s?retryWrites=%s&w=%s";

            String uri = String.format(
                    uriFormat,
                    props.getUsername(),
                    props.getPassword(),
                    props.getEndpoint(),
                    props.getAuthDatabase(),
//                    props.getEnableTLS(),
                    props.getRetryWrites(),
                    props.getWriteMode());

            return uri;
        }
    }

    public @Bean MongoClientFactoryBean mongo() {
        ConnectionString connectionString = new ConnectionString(this.getMongoURI());

        MongoClientFactoryBean mongo = new MongoClientFactoryBean();

        mongo.setConnectionString(connectionString);

        return mongo;
    }
}
