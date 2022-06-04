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

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;

//http://localhost:5051/api/demos/lista
//http://localhost:5051/api/demos/lista/api-docs

@RestController
@RequestMapping("api/demoms")
public class ExampleMicro5dynamoController {

  @Autowired
  ExampleMicro5dynamoService service;

  @Operation(summary = "Get a list", tags = "getList")
  @ApiResponses(value = { 
		  @ApiResponse(responseCode = "200", description = "Found elements", 
		    content = { @Content(mediaType = "application/json", 
		      schema = @Schema( implementation = ArrayList.class)) }),
		  @ApiResponse(responseCode = "404", description = "None element found",content = @Content) })
  @GetMapping(value= "/lista", produces= "application/json")
  public ResponseEntity<Iterable<ExampleMicro5dynamoEntity>> lista(){
    Iterable<ExampleMicro5dynamoEntity> l=service.findAll();
    if (l==null )
    	return new ResponseEntity<Iterable<ExampleMicro5dynamoEntity>> ( new ArrayList<ExampleMicro5dynamoEntity>() ,HttpStatus.NOT_FOUND);
    return new ResponseEntity<Iterable<ExampleMicro5dynamoEntity>> ( l ,HttpStatus.OK);
  }

  @Operation(summary = "Save a new object or update existing if id already exist")
  @PostMapping(value= "/save", produces= "application/json")
  public ResponseEntity<ExampleMicro5dynamoEntity> save(
		  @io.swagger.v3.oas.annotations.parameters.RequestBody(
                  description = "Element of ExampleMicro5dynamoEntity",
                  required=true,
                  content = { @Content(mediaType = "application/json", 
    		      	schema = @Schema(implementation = ExampleMicro5dynamoEntity.class)) }
                 )
		  @Validated @RequestBody ExampleMicro5dynamoEntity el){
	  el=service.save(el);	  
	  return new ResponseEntity<ExampleMicro5dynamoEntity> ( el ,HttpStatus.OK);
  }
  
}