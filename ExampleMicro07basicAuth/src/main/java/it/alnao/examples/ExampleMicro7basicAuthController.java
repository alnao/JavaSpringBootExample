package it.alnao.examples;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("api/demoms")
public class ExampleMicro7basicAuthController {
	  @Autowired
	  ExampleMicro7basicAuthService service;
	
	  @GetMapping(value= "/lista", produces= "application/json")
	  public ResponseEntity<List<ExampleMicro7basicAuthEntity>> lista(){
	    return new ResponseEntity<List<ExampleMicro7basicAuthEntity>> ( service.findAll() ,HttpStatus.OK);
	  }

	  @GetMapping(value= "/amministrativa", produces= "application/json")
	  public ResponseEntity<List<String>> listAmministrativa(){
	    ArrayList<String> l=new ArrayList<String>();
	    l.add("uno admin");
	    l.add("due admin");
	    return new ResponseEntity<List<String>> ( l ,HttpStatus.OK);
	  }
	  
	  @PostMapping(value= "/save", produces= "application/json")
	  public ResponseEntity<ExampleMicro7basicAuthEntity> save(@Validated @RequestBody ExampleMicro7basicAuthEntity el){
		  el=service.save(el);	  
		  return new ResponseEntity<ExampleMicro7basicAuthEntity> ( el ,HttpStatus.OK);
	  }
	  
}
