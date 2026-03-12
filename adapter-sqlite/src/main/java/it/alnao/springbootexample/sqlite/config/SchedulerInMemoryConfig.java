package it.alnao.springbootexample.sqlite.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.inmemory.InMemoryLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("sqlite")
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
public class SchedulerInMemoryConfig {

    @Bean
    public LockProvider lockProvider() {
        return new InMemoryLockProvider();
        // works only within a single JVM — fine for sqlite (single instance by design)
    }
}