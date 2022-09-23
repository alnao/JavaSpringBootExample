package it.alnao.examples;

import java.util.Date;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class ExampleMicro4mongoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExampleMicro4mongoApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(ExampleMicro4mongoReposiroty r) {
		return args -> {
			r.save(new ExampleMicro4mongoEntity("1","Avviato il " + new Date()));
			System.out.println("Elenco elementi nel ExampleMicro4mongo ");
			for ( var el : r.findAll() ) {
				System.out.println(el.toString());
			}
		};
	}
}
