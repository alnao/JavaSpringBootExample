package it.alnao.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/sendOrder/")
public class ExampleMicro11asyncProducerController {

	@Autowired
    private ExampleMicro11asyncProducerService exampleMicro11asyncProducerService;

    //@Value("${ExampleMicro11asyncProducerController.message}")
    private String message="OK";

    @PostMapping(value = "ordine")
    public String publishUserDetails(@RequestBody ExampleMicro11asyncOrdineEntity user) {
    	exampleMicro11asyncProducerService.send(user);
        return message;
    }
}

