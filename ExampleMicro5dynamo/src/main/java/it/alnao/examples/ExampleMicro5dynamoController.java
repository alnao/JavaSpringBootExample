package it.alnao.examples;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//http://localhost:5051/api/demos/lista

@RestController
@RequestMapping("api/demoms")
public class ExampleMicro5dynamoController {

  @Autowired
  ExampleMicro5dynamoService service;

  @GetMapping(value= "/lista", produces= "application/json")
  public ResponseEntity<Iterable<ExampleMicro5dynamoEntity>> lista(){
    Iterable<ExampleMicro5dynamoEntity> l=service.findAll();
    if (l==null )
    	return new ResponseEntity<Iterable<ExampleMicro5dynamoEntity>> ( new ArrayList<ExampleMicro5dynamoEntity>() ,HttpStatus.NOT_FOUND);
    return new ResponseEntity<Iterable<ExampleMicro5dynamoEntity>> ( l ,HttpStatus.OK);
  }

  @PostMapping(value= "/save", produces= "application/json")
  public ResponseEntity<ExampleMicro5dynamoEntity> save(@Validated @RequestBody ExampleMicro5dynamoEntity el){
	  el=service.save(el);	  
	  return new ResponseEntity<ExampleMicro5dynamoEntity> ( el ,HttpStatus.OK);
  }
  
}