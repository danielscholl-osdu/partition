package org.opengroup.osdu.partition.provider.azure.di;

import com.azure.security.keyvault.secrets.SecretClient;
import com.lambdaworks.redis.ClientOptions;
import com.lambdaworks.redis.SocketOptions;
import org.opengroup.osdu.azure.KeyVaultFacade;
import org.opengroup.osdu.core.common.cache.RedisCache;
import org.opengroup.osdu.partition.model.PartitionInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Named;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class RedisConfig {

    @Bean
    @Named("REDIS_HOST")
    public String redisHost(SecretClient kv) {
        return KeyVaultFacade.getSecretWithValidation(kv, "redis-hostname");
    }

    @Bean
    @Named("REDIS_PASSWORD")
    public String redisPassword(SecretClient kv) {
        return KeyVaultFacade.getSecretWithValidation(kv, "redis-password");
    }

    @Configuration
    @ConditionalOnExpression(value = "'${cache.provider}' == 'redis' && '${redis.ssl.enabled:true}'")
    static class SslConfig {

        @Value("${redis.port}")
        private int port;

        @Value("${redis.expiration}")
        private int expiration;

        @Value("${redis.database}")
        private int database;

        @Bean
        public RedisCache<String, PartitionInfo> partitionServiceCache(@Named("REDIS_HOST") String host, @Named("REDIS_PASSWORD") String password) {
            ClientOptions clientOptions = ClientOptions.builder()
                    .socketOptions(SocketOptions.builder().connectTimeout(15, TimeUnit.SECONDS).build())
                    .build();
            return new RedisCache<>(host, port, password, expiration, database, clientOptions, String.class, PartitionInfo.class);
        }

        @Bean
        public RedisCache<String, List<String>> partitionListCache(@Named("REDIS_HOST") String host, @Named("REDIS_PASSWORD") String password) {
            ClientOptions clientOptions = ClientOptions.builder()
                    .socketOptions(SocketOptions.builder().connectTimeout(15, TimeUnit.SECONDS).build())
                    .build();
            return new RedisCache(host, port, password, expiration, database, clientOptions, String.class, List.class);
        }

    }

    @Configuration
    @ConditionalOnExpression(value = "'${cache.provider}' == 'redis' && !'${redis.ssl.enabled:true}'")
    static class NoSslConfig {

        @Value("${redis.port}")
        private int port;

        @Value("${redis.expiration}")
        private int expiration;

        @Value("${redis.database}")
        private int database;

        @Bean
        public RedisCache<String, PartitionInfo> partitionServiceCache(@Named("REDIS_HOST") String host) {
            ClientOptions clientOptions = ClientOptions.builder()
                    .socketOptions(SocketOptions.builder().connectTimeout(15, TimeUnit.SECONDS).build())
                    .build();
            return new RedisCache<>(host, port, expiration, database, clientOptions, String.class, PartitionInfo.class);
        }

        @Bean
        public RedisCache<String, List<String>> partitionListCache(@Named("REDIS_HOST") String host) {
            ClientOptions clientOptions = ClientOptions.builder()
                    .socketOptions(SocketOptions.builder().connectTimeout(15, TimeUnit.SECONDS).build())
                    .build();
            return new RedisCache(host, port, expiration, database, clientOptions, String.class, List.class);
        }

    }
}
