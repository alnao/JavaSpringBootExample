package it.alnao.examples;

import java.util.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@EnableAutoConfiguration
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ExampleMicro20mockitoApplication{
	
	public static void main(String[] args) {
		SpringApplication.run(ExampleMicro20mockitoApplication.class, args);
	}


}
