package it.alnao.examples;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
//import lombok.Data;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("api/demoms")
public class ExampleMicro9feignController {

    @RequestMapping("/")
    String hello() {
        return "Hello World!";
    }
/*
    @Autowired
    ExampleMicro9feignMagazzinoInterface exampleMicro9feignMagazzinoInterface;

    @GetMapping(value ="/magazzinoAPIEsterna/{codArt}")
	public ResponseEntity<List<String>> magazzino(
		@RequestHeader("Authorization") String authHeader,
	  	@PathVariable("codArt") String codArt){
            System.out.println("magazzinoAPIEsterna codArt=" + codArt);
            return exampleMicro9feignMagazzinoInterface.magazzino(authHeader,codArt);
    };
*/
    
    @Autowired
    ExampleMicro9feignService service;
    
    @GetMapping(value= "/lista", produces= "application/json")
    public ResponseEntity<Iterable<ExampleMicro9feignEntityWithMagazzino>> lista(@RequestHeader("Authorization") String authHeader){
      Iterable<ExampleMicro9feignEntityWithMagazzino> l=service.findAll(authHeader);
      if (l==null )
          return new ResponseEntity<Iterable<ExampleMicro9feignEntityWithMagazzino>> ( new ArrayList<ExampleMicro9feignEntityWithMagazzino>() ,HttpStatus.NOT_FOUND);
      return new ResponseEntity<Iterable<ExampleMicro9feignEntityWithMagazzino>> ( l ,HttpStatus.OK);
    }

    @PostMapping(value= "/save", produces= "application/json")
    public ResponseEntity<ExampleMicro9feignEntity> listaComuni(@Validated @RequestBody ExampleMicro9feignEntity el){
        el=service.save(el);    
        return new ResponseEntity<ExampleMicro9feignEntity> ( el ,HttpStatus.OK);
    }

}
