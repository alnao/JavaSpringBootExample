package it.alnao.esempio02db.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.alnao.esempio02db.ResourceNotFoundException;
import it.alnao.esempio02db.entity.ExampleMicro2dbEntity;
import it.alnao.esempio02db.repository.ExampleMicro2dbRepository;

@Service
public class ExampleMicro2dbService {
    @Autowired
    private ExampleMicro2dbRepository repository;
    
    public List<ExampleMicro2dbEntity> findAll() {
        return repository.findAll();
    }
    
    public ExampleMicro2dbEntity findById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }
    
    public ExampleMicro2dbEntity save(ExampleMicro2dbEntity product) {
        return repository.save(product);
    }
    
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
