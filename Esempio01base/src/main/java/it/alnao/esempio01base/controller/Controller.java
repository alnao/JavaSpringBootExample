package it.alnao.esempio01base.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("api")
public class Controller {
    Logger logger = LoggerFactory.getLogger(Controller.class);
	
    @RequestMapping("/")
    String hello() {
    	logger.debug("hello api called");
        return "Hello World from Example01 by AlNao!";
    }
    // http://locahost:5051/api/
    
	@GetMapping(value= "/response", produces= "application/json")
	public ResponseEntity<String> response(){
		logger.debug("response api called");
		return new ResponseEntity<String> ("{\"response\":\"ok\"}",HttpStatus.OK)  ;
	}
	// http://locahost:5051/api/response
}