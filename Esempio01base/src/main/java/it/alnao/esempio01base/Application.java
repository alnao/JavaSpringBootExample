package it.alnao.esempio01base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import it.alnao.esempio01base.controller.Controller;

//@SpringBootApplication
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class Application {
	Logger logger = LoggerFactory.getLogger(Controller.class);
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	@Bean
	public CommandLineRunner startMethod(/*Repository r*/) {
		return args -> {
			//r.save(new Entity("1","Started at " + new Date()));
			logger.debug("ExampleMicro1Application startMethod");
		};
	}
}
