package it.alnao.examples;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;


import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name="ExampleMicro8gestJwt", url="localhost:5051/api/demoms")
//name = spring.application.name del client
//port solo una per volta per ora
public interface ExampleMicro9feignMagazzinoInterface{
    //metodi
	@GetMapping(value ="/magazzino/{codArt}")
	public ResponseEntity<List<String>> magazzino(
		@RequestHeader("Authorization") String authHeader,
	  	@PathVariable("codArt") String codArt)
    ;


}