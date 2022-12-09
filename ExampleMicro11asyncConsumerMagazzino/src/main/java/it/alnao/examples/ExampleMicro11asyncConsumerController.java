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
public class ExampleMicro11asyncConsumerController {
  @Autowired
  ExampleMicro11asyncConsumerService service;

  @GetMapping(value= "/lista", produces= "application/json")
  public ResponseEntity<Iterable<ExampleMicro11asyncConsumerMagazzinoEntity>> lista(){
    Iterable<ExampleMicro11asyncConsumerMagazzinoEntity> l=service.findAll();
    if (l==null )
    	return new ResponseEntity<Iterable<ExampleMicro11asyncConsumerMagazzinoEntity>> ( new ArrayList<ExampleMicro11asyncConsumerMagazzinoEntity>() ,HttpStatus.NOT_FOUND);
    return new ResponseEntity<Iterable<ExampleMicro11asyncConsumerMagazzinoEntity>> ( l ,HttpStatus.OK);
  }


}