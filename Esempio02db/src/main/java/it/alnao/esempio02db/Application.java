package it.alnao.esempio02db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
//import it.alnao.esempio02db.controller.ExampleMicro2dbController;
import org.springframework.context.annotation.Bean;

//@SpringBootApplication
//@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@SpringBootApplication(scanBasePackages={"it.alnao.esempio02db.service","it.alnao.esempio02db.repository","it.alnao.esempio02db.controller"})
public class Application {
	Logger logger = LoggerFactory.getLogger(Application.class);
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	@Bean
	public CommandLineRunner startMethod(/*Repository r*/) {
		return args -> {
			//r.save(new Entity("1","Started at " + new Date()));
			logger.debug("Esempio02db startMethod");
		};
	}
}
