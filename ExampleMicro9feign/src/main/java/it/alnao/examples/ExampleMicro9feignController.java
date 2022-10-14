package it.alnao.examples;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
//import lombok.Data;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequestMapping("api/demoms")
public class ExampleMicro9feignController {
    @Autowired
    ExampleMicro9feignMagazzinoInterface exampleMicro9feignMagazzinoInterface;

    @RequestMapping("/")
    String hello() {
        return "Hello World!";
    }

    @GetMapping(value ="/magazzinoAPIEsterna/{codArt}")
	public ResponseEntity<List<String>> magazzino(
		@RequestHeader("Authorization") String authHeader,
	  	@PathVariable("codArt") String codArt){
            System.out.println("magazzinoAPIEsterna codArt=" + codArt);
            return exampleMicro9feignMagazzinoInterface.magazzino(authHeader,codArt);
    };


}
