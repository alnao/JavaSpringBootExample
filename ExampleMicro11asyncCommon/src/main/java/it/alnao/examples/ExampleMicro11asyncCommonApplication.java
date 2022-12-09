package it.alnao.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ExampleMicro11asyncCommonApplication {
	/*
	public static void main(String[] args) {
		SpringApplication.run(ExampleMicro11asyncCommonApplication.class, args);
	}*/

	@Autowired
	ExampleMicro11asyncCloudConfig config; //config del Spring Cloud
	
	@Bean
	public CommandLineRunner demo(ExampleMicro11asyncCloudConfig config) {
		return args -> {
			System.out.println("Configurazione del ExampleMicro11asyncCloudConfig "+ config);
		};
	}
}
