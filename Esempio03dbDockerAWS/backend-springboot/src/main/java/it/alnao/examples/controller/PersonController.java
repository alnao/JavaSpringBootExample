package it.alnao.examples.controller;
import it.alnao.examples.model.Person;
import it.alnao.examples.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;

@CrossOrigin(origins = "*") //@CrossOrigin(origins = "http://localhost:5084")
@RestController
@RequestMapping("/api/persone")
public class PersonController {

    @Autowired
    private PersonRepository personRepo;

    @GetMapping("")
    public List<Person> getAll() {
        return personRepo.findAll();
    }

    @PostMapping("")
    public Person create(@RequestBody Person p) {
        return personRepo.save(p);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Person> update(@PathVariable Long id, @RequestBody Person newPerson) {
        return personRepo.findById(id)
                .map(person -> {
                    person.setNome(newPerson.getNome());
                    person.setCognome(newPerson.getCognome());
                    person.setEta(newPerson.getEta());
                    return ResponseEntity.ok(personRepo.save(person));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!personRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        personRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @Value("${app.message}")
    private String message;
    @Value("${app.mysql}")
    private String mysql;

    @GetMapping("/info")
    public String getInfo() throws UnknownHostException {
        InetAddress address = InetAddress.getByName(mysql); 
        System.out.println(address.getHostAddress());
        return "Messaggio caricato: " + message + " da: " + address.getHostAddress() + " - " + address.getHostName();
    }
}