package it.alnao.springbootexample.redis.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@Profile({"kube", "aws", "azure"})
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
public class SchedulerRedisConfig { //ex ShedLockConfigRedis

    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        return new RedisLockProvider(connectionFactory, "scheduler-send-annotations");
    }

}