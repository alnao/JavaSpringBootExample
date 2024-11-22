package it.alnao.esempio02db.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.alnao.esempio02db.entity.ExampleMicro2dbEntity;
import it.alnao.esempio02db.service.ExampleMicro2dbService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;


@RestController
@RequestMapping("api/articoli")
public class ExampleMicro2dbController {
    Logger logger = LoggerFactory.getLogger(ExampleMicro2dbController.class);
    @Autowired
    private ExampleMicro2dbService service;
    
    @GetMapping(value="/check")
    public String check() {//@RequestParam String param
        return new String("OK");
    }
    
    @GetMapping(value="/all")
    public List<ExampleMicro2dbEntity> getAll() {
        return service.findAll();
    }
    
    @GetMapping("/{id}")
    public ExampleMicro2dbEntity getById(@PathVariable Long id) {
        return service.findById(id);
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExampleMicro2dbEntity create(@RequestBody ExampleMicro2dbEntity product) {
        return service.save(product);
    }
    
    @PutMapping("/{id}")
    public ExampleMicro2dbEntity update(@PathVariable Long id, @RequestBody ExampleMicro2dbEntity product) {
        product.setId(id);
        return service.save(product);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteById(id);
    }
}