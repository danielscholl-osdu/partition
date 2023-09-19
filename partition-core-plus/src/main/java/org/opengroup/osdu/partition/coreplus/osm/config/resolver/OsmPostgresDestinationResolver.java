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

package org.opengroup.osdu.partition.coreplus.osm.config.resolver;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opengroup.osdu.core.osm.core.model.Destination;
import org.opengroup.osdu.core.osm.postgresql.PgDestinationResolution;
import org.opengroup.osdu.core.osm.postgresql.PgDestinationResolver;
import org.opengroup.osdu.partition.coreplus.config.PostgresOsmConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON;

@Primary
@Component
@Scope(SCOPE_SINGLETON)
@ConditionalOnProperty(name = "osmDriver", havingValue = "postgres")
@RequiredArgsConstructor
@Slf4j
public class OsmPostgresDestinationResolver implements PgDestinationResolver {

    private final PostgresOsmConfiguration properties;

    private static final String DRIVER_CLASS_NAME = "org.postgresql.Driver";

    private static DataSource dataSource;
    @Override
    public PgDestinationResolution resolve(Destination destination) {
        if (dataSource == null || (dataSource instanceof HikariDataSource && ((HikariDataSource) dataSource).isClosed())) {
            dataSource = buildDataSourceFromProperties();
        }

        return PgDestinationResolution.builder()
            .datasource(dataSource)
            .build();
    }

    private DataSource buildDataSourceFromProperties() {
        DataSource dataSource;
        dataSource = DataSourceBuilder.create()
            .driverClassName(DRIVER_CLASS_NAME)
            .url(properties.getUrl())
            .username(properties.getUsername())
            .password(properties.getPassword())
            .build();

        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        hikariDataSource.setMaximumPoolSize(properties.getMaximumPoolSize());
        hikariDataSource.setMinimumIdle(properties.getMinimumIdle());
        hikariDataSource.setIdleTimeout(properties.getIdleTimeout());
        hikariDataSource.setMaxLifetime(properties.getMaxLifetime());
        hikariDataSource.setConnectionTimeout(properties.getConnectionTimeout());
        return dataSource;
    }

    @PreDestroy
    @Override
    public void close() {
        log.info("On pre-destroy. {} shutting down the datasource");
        if (dataSource instanceof HikariDataSource && !((HikariDataSource) dataSource).isClosed()) {
            ((HikariDataSource) dataSource).close();
        }
    }
}
