package it.alnao.examples;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/demoms")
public class ExampleMicro2dbController {
  @Autowired
  ExampleMicro2dbService service;

  @GetMapping(value= "/lista", produces= "application/json")
  public ResponseEntity<List<ExampleMicro2dbEntity>> listaComuni(){
    List<ExampleMicro2dbEntity> l=service.getAll();
    if (l==null || l.size()==0)
    return new ResponseEntity<List<ExampleMicro2dbEntity>> ( new ArrayList<ExampleMicro2dbEntity>() ,HttpStatus.NOT_FOUND);
    return new ResponseEntity<List<ExampleMicro2dbEntity>> ( l ,HttpStatus.OK);
  }

}