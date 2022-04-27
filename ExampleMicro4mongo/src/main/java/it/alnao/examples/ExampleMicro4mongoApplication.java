package it.alnao.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ExampleMicro4mongoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExampleMicro4mongoApplication.class, args);
	}

}
