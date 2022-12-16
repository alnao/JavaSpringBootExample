package it.alnao.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableEurekaServer
@SpringBootApplication() //exclude = {DataSourceAutoConfiguration.class })
@RestController
public class ExampleMicro12eurekaServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExampleMicro12eurekaServerApplication.class, args);
	}

   @RequestMapping("/")
   public String home() {
	   return "ExampleMicro12eurekaServerApplication";
   }
}
