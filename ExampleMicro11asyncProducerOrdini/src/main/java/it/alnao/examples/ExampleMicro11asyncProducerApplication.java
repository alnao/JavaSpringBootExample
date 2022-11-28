package it.alnao.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@PropertySources({ //https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config
    @PropertySource("classpath:bootstrap.properties"),
    @PropertySource("classpath:application.properties")
})
public class ExampleMicro11asyncProducerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExampleMicro11asyncProducerApplication.class, args);
	}
	
	@Autowired
	ExampleMicro11asyncCloudConfig config; //config del Spring Cloud
	/*
    @Value("${spring.rabbitmq.host}")
    String host;
    @Value("${spring.rabbitmq.username}")
    String username;
    @Value("${spring.rabbitmq.password}")
    String password;
    */

    @Bean
    CachingConnectionFactory connectionFactory() {
    	CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(config.getHost() );
        cachingConnectionFactory.setUsername(config.getUsername() );
        cachingConnectionFactory.setPassword(config.getPassword() );
        return cachingConnectionFactory;
    }
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);// new RabbitTemplate(connectionFactory)
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
	

}
