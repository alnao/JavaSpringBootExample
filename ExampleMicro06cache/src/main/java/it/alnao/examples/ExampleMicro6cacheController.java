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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("api/demoms")
public class ExampleMicro6cacheController {
  @Autowired
  ExampleMicro6cacheService service;

  @GetMapping(value= "/lista", produces= "application/json")
  public ResponseEntity<List<ExampleMicro6cacheEntity>> listaComuni(){
    List<ExampleMicro6cacheEntity> l=service.getAll();
    if (l==null || l.size()==0)
    return new ResponseEntity<List<ExampleMicro6cacheEntity>> ( new ArrayList<ExampleMicro6cacheEntity>() ,HttpStatus.NOT_FOUND);
    return new ResponseEntity<List<ExampleMicro6cacheEntity>> ( l ,HttpStatus.OK);
  }
  
  @PostMapping(value= "/save", produces= "application/json")
  public ResponseEntity<ExampleMicro6cacheEntity> save(ExampleMicro6cacheEntity el){
	  el=service.save(el);	  
	  return new ResponseEntity<ExampleMicro6cacheEntity> ( el ,HttpStatus.OK);
  }
  
  @GetMapping("clearCache")
  public void clearCache() {
	  service.clearCache();	  
  }
}