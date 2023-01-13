package it.alnao.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/demoms")
public class ExampleMicro17turbineController {
	@Autowired 
	ExampleMicro17turbineService service;
	
	@GetMapping(value= "/temperatura", produces= "application/json")
	public ResponseEntity<String> lista( @RequestParam(name = "anno") String anno) {
	    String temp;
		try {
			temp = service.getTemperaturaMedia( Integer.valueOf( anno) );
		} catch (NumberFormatException e) {
			temp ="NumberFormatException";
			e.printStackTrace();
		} catch (InterruptedException e) {
			temp ="InterruptedException";
			e.printStackTrace();
		}
	    return new ResponseEntity<String> ( temp ,HttpStatus.OK);
	  }
}
