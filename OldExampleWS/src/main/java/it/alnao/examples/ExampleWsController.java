package it.alnao.examples;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api") //endpoint
@CrossOrigin(origins="http://localhost:4200")
public class ExampleWsController {
	
	@GetMapping(value = "/saluti") //httpGET e urlRisorsa
	public String getSaluti() {
		return "AlNao Example WS";
	}
	
	@GetMapping(value = "/saluti2/{nome}") //httpGET e urlRisorsa
	public String getSaluti2(@PathVariable("nome") String nome) {
		if ("Marco".equals(nome)) {
			throw new RuntimeException("Marco not enabled");
		}
		return "\"Saluti a "+nome+"\"";
	}
}
