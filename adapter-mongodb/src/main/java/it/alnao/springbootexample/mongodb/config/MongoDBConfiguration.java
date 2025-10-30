package it.alnao.springbootexample.mongodb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@Profile("kube")
@EnableMongoRepositories(basePackages = "it.alnao.springbootexample.mongodb.repository")
public class MongoDBConfiguration {
}
