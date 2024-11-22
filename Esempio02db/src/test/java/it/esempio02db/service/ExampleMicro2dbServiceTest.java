package it.esempio02db.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.alnao.esempio02db.ResourceNotFoundException;
import it.alnao.esempio02db.entity.ExampleMicro2dbEntity;
import it.alnao.esempio02db.repository.ExampleMicro2dbRepository;
import it.alnao.esempio02db.service.ExampleMicro2dbService;

@ExtendWith(MockitoExtension.class)
class ExampleMicro2dbServiceTest {
    @Mock
    private ExampleMicro2dbRepository repository;
    
    @InjectMocks
    private ExampleMicro2dbService service;
    
    @Test
    void findAll_ShouldReturnAllProducts() {
        List<ExampleMicro2dbEntity> products = Arrays.asList(
            new ExampleMicro2dbEntity(1L, "Product1", "10"),
            new ExampleMicro2dbEntity(2L, "Product2", "42")
        );
        when(repository.findAll()).thenReturn(products);
        
        assertEquals(products, service.findAll());
    }
    
    @Test
    void findById_WhenExists_ShouldReturnProduct() {
        ExampleMicro2dbEntity product = new ExampleMicro2dbEntity(1L, "Product1", "42");
        when(repository.findById(1L)).thenReturn(Optional.of(product));
        
        assertEquals(product, service.findById(1L));
    }
    
    @Test
    void findById_WhenNotExists_ShouldThrowException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> service.findById(1L));
    }
    
    @Test
    void save_ShouldReturnSavedProduct() {
        ExampleMicro2dbEntity product = new ExampleMicro2dbEntity(1L, "Product1", "42");
        when(repository.save(product)).thenReturn(product);
        
        assertEquals(product, service.save(product));
    }
}
    