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
public class ExampleMicro4mongoController {
  @Autowired
  ExampleMicro4mongoService service;

  @GetMapping(value= "/lista", produces= "application/json")
  public ResponseEntity<Iterable<ExampleMicro4mongoEntity>> listaComuni(){
    Iterable<ExampleMicro4mongoEntity> l=service.findAll();
    if (l==null )
    	return new ResponseEntity<Iterable<ExampleMicro4mongoEntity>> ( new ArrayList<ExampleMicro4mongoEntity>() ,HttpStatus.NOT_FOUND);
    return new ResponseEntity<Iterable<ExampleMicro4mongoEntity>> ( l ,HttpStatus.OK);
  }

  @PostMapping(value= "/save", produces= "application/json")
  public ResponseEntity<ExampleMicro4mongoEntity> listaComuni(@Validated @RequestBody ExampleMicro4mongoEntity el){
	  el=service.save(el);	  
	  return new ResponseEntity<ExampleMicro4mongoEntity> ( el ,HttpStatus.OK);
  }
  
}