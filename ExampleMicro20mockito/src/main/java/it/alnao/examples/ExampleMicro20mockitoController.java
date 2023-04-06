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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class ExampleMicro20mockitoController {
  @Autowired
  ExampleMicro20mockitoService service;
	
  ///api/valore?currency=EUR&coin=1
  @GetMapping(value= "/valore", produces= "application/json")
  public ResponseEntity<Double> valore(@RequestParam String currency, @RequestParam String coin){
    double val=service.convertBitcoins(currency, Double.valueOf(coin));
    return new ResponseEntity<Double> ( Double.valueOf(val) ,HttpStatus.OK);
  }

}