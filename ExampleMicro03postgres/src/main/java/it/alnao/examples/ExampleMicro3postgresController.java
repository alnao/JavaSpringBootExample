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

@RestController
@RequestMapping("api/demoms")
public class ExampleMicro3postgresController {
  @Autowired
  ExampleMicro3postgresService service;

  @GetMapping(value= "/lista", produces= "application/json")
  public ResponseEntity<Iterable<ExampleMicro3postgresEntity>> listaComuni(){
    Iterable<ExampleMicro3postgresEntity> l=service.getAll();
    if (l==null )
    	return new ResponseEntity<Iterable<ExampleMicro3postgresEntity>> ( new ArrayList<ExampleMicro3postgresEntity>() ,HttpStatus.NOT_FOUND);
    return new ResponseEntity<Iterable<ExampleMicro3postgresEntity>> ( l ,HttpStatus.OK);
  }

  @PostMapping(value= "/save", produces= "application/json")
  public ResponseEntity<ExampleMicro3postgresEntity> listaComuni(@Validated @RequestBody ExampleMicro3postgresEntity el){
	  el=service.save(el);	  
	  return new ResponseEntity<ExampleMicro3postgresEntity> ( el ,HttpStatus.OK);
  }
  
}