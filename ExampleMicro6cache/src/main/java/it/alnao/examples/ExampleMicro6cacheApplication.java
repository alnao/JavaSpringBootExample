package it.alnao.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ExampleMicro6cacheApplication {

	public static void main(String[] args) {

		SpringApplication.run(ExampleMicro6cacheApplication.class, args);
	}

}
