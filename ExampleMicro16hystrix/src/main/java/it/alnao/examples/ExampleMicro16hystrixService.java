package it.alnao.examples;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

@Service
//@EnableHitrix
public class ExampleMicro16hystrixService {
	public static final String FailueTimeOutMs = "4000";//costante HYSTRIX per il timeout
	
	//@HystrixCommand(fallbackMethod = "temperaturaGenerica")
	@HystrixCommand(fallbackMethod="temperaturaGenerica", threadPoolKey = "prim",
			commandProperties = {
		            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", 
		            value = FailueTimeOutMs)
		    }
	)
	String getTemperaturaMedia(Integer anno) throws InterruptedException {
		if (anno > 2023)
			throw new IllegalArgumentException("Anno futuro!");
		switch (anno) { 
			case 2022 : return "Il più caldo";
			case 2021 : return "Caldo";
			case 2020 : return "Covid";
			case 2019 : return "Caldo";
			case 2018 : return "Caldino";
			case 2017 : return "Fresco";
			case 2013 : return "Caldissimo";
	    }
		int randomMS=ThreadLocalRandom.current().nextInt(2000,8000);//tra i 2 e 8 s
		System.out.println("ExampleMicro16hystrixService wait time="+randomMS);
		Thread.sleep(randomMS);
		return "Normale";
		//throw new IllegalArgumentException("Anno futuro!");
	}

	String temperaturaGenerica(Integer anno) {
		System.out.println("ExampleMicro16hystrixService temperaturaGenerica");
	    return "Generico";
	}
}
