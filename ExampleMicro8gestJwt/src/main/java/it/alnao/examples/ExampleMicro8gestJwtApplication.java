package it.alnao.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ExampleMicro8gestJwtApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExampleMicro8gestJwtApplication.class, args);
	}

}
