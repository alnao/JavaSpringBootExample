package it.alnao.examples;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ExampleMicro10cloudConfigApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExampleMicro10cloudConfigApplication.class, args);
	} 

}
