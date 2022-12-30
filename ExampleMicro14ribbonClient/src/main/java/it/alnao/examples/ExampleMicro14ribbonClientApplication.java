package it.alnao.examples;

import org.apache.http.HttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
//import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
//import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.netflix.ribbon.RibbonClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RibbonClient( name = "ExampleMicro14ribbon", configuration = ExampleMicro14ribbonConfiguration.class) // For Ribbon
@EnableDiscoveryClient
//@EnableCircuitBreaker // For Hystrix and Ribbon
@SpringBootApplication(scanBasePackages={"com.netflix.client.config.IClientConfig"})
@RestController
public class ExampleMicro14ribbonClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExampleMicro14ribbonClientApplication.class, args);
	}

    @RequestMapping("/ExampleMicro14ribbonClient")
    public String home() {
    	String r="";
    	ServiceInstance serviceInstance=loadBalancer.choose("ExampleMicro14ribbon");
    	//System.out.println(serviceInstance.getUri());
		String baseUrl=serviceInstance.getUri().toString();
		baseUrl=baseUrl+"/ExampleMicro14ribbon";
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response=null;
		try{
			response=restTemplate.exchange(baseUrl,
				HttpMethod.GET, getHeaders(),String.class);
			r=response.getBody() ;
		}catch (Exception ex){
			System.out.println(ex);
			r="Error : " + ex.getMessage();
		}
    	return "ExampleMicro14ribbonClientApplication from " + r ;
    	
    }

	@Autowired
	private LoadBalancerClient loadBalancer;
	
	private static org.springframework.http.HttpEntity<?> getHeaders() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		return new org.springframework.http.HttpEntity<>(headers);
	} 
	/* */
	
}
