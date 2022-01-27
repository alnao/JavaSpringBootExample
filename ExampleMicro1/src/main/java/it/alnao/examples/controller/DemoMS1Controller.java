package it.alnao.examples.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("api/demoms")
public class DemoMS1Controller {
	@GetMapping(value= "/riposta", produces= "application/json")
	public ResponseEntity<String> listArtByEan(){
		return new ResponseEntity<String> ("riposta",HttpStatus.OK);
	}
	//http://localhost:5051/api/demoms/riposta
}