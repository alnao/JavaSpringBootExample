package it.alnao.examples.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("api/demoms")
public class DemoMS1Controller {
    Logger logger = LoggerFactory.getLogger(DemoMS1Controller.class);
	
	@GetMapping(value= "/response", produces= "application/json")
	public ResponseEntity<String> response(){
		logger.debug("response api called");
		return new ResponseEntity<String> ("{\"response\":\"ok\"}",HttpStatus.OK)  ;
	}
	//http://localhost:5051/api/demoms/response
}