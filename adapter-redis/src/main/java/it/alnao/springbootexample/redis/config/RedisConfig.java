package it.alnao.springbootexample.redis.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configurazione Redis per lock distribuiti.
 * Attiva per profili kube, aws, azure.
 */
@Configuration
@Profile({"kube", "aws", "azure"})
public class RedisConfig {
    
    @Value("${spring.redis.data.host:localhost}")
    private String redisHost;
    
    @Value("${spring.redis.data.port:6379}")
    private int redisPort;
    
    @Value("${spring.redis.data.password:}")
    private String redisPassword;
    
    @Value("${spring.redis.data.ssl:false}")
    private boolean redisSsl;
    
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        String address = String.format("%s://%s:%d", 
            redisSsl ? "rediss" : "redis", 
            redisHost, 
            redisPort);
        
        config.useSingleServer()
            .setAddress(address)
            .setConnectionMinimumIdleSize(2)
            .setConnectionPoolSize(10)
            .setTimeout(3000)
            .setRetryAttempts(3)
            .setRetryInterval(1500);
        
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.useSingleServer().setPassword(redisPassword);
        }
        
        return Redisson.create(config);
    }
}
