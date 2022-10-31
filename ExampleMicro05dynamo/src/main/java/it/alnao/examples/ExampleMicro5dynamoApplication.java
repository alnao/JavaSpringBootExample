package it.alnao.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ExampleMicro5dynamoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExampleMicro5dynamoApplication.class, args);
	}

}
