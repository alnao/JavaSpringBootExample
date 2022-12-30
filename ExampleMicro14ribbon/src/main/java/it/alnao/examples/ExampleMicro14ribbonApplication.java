package it.alnao.examples;

//import org.apache.log4j.Logger;
import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages={"com.netflix.client.config.IClientConfig"})
@RestController
@RibbonClient(name="ExampleMicro14ribbon", configuration = ExampleMicro14ribbonConfiguration.class)
public class ExampleMicro14ribbonApplication {
	@Value("${server.port}")  
	String port;
	public static void main(String[] args) {
		SpringApplication.run(ExampleMicro14ribbonApplication.class, args);
	}
    @RequestMapping("/ExampleMicro14ribbon")
    public String home() {
    	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");  
    	LocalDateTime now = LocalDateTime.now();  
    	System.out.println( dtf.format(now) + " ExampleMicro14ribbon on port " + port);  
    	return "ExampleMicro14ribbonApplication "+ port;
    }
}
