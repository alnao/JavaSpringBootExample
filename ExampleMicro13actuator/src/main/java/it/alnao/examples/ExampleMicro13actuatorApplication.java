package it.alnao.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableDiscoveryClient
@SpringBootApplication
@RestController
public class ExampleMicro13actuatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExampleMicro13actuatorApplication.class, args);
	}
	   @RequestMapping("/")
	   public String home() {
		   return "ExampleMicro13actuatorApplication";
	   }
	   @RequestMapping("/ExampleMicro13actuator")
	   public String exampleMicro13actuator() {
		   return "ExampleMicro13actuator";
	   }
}
