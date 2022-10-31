package it.alnao.examples;

import java.util.List;

import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.hazelcast.config.Config;
import com.hazelcast.config.TcpIpConfig;

@Configuration
public class ExampleMicro6cacheConfig {

    @Bean
    public Config config() {
	    Config cfg = new Config();
	    final TcpIpConfig tcpIpConfig = cfg.getNetworkConfig().getJoin().getTcpIpConfig();
	    tcpIpConfig.setEnabled(true);
	    tcpIpConfig.setMembers(List.of("127.0.0.1"));
	    return cfg;
    }

}