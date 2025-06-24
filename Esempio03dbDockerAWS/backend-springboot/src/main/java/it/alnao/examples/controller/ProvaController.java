package it.alnao.examples.controller;

//import java.net.InetAddress;
import java.net.UnknownHostException;

//import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*") //@CrossOrigin(origins = "http://localhost:5084")
@RestController
@RequestMapping("/api/prova")
public class ProvaController {

    @GetMapping("/info")
    public String getInfo() throws UnknownHostException {
        System.out.println("AAA");
        return "Messaggio caricato: ";
    }
}
