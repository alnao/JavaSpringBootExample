package it.esempio02db.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import it.alnao.esempio02db.ResourceNotFoundException;
import it.alnao.esempio02db.controller.ExampleMicro2dbController;
import it.alnao.esempio02db.entity.ExampleMicro2dbEntity;
import it.alnao.esempio02db.repository.ExampleMicro2dbRepository;
import it.alnao.esempio02db.service.ExampleMicro2dbService;

@ExtendWith(MockitoExtension.class)
class ExampleMicro2dbControllerTest {
    @Mock
    private ExampleMicro2dbService service;
    
    @InjectMocks
    private ExampleMicro2dbController controller;
    
    @Test
    void getAll_ShouldReturnAllExampleMicro2dbs() {
        List<ExampleMicro2dbEntity> ExampleMicro2dbs = Arrays.asList(
            new ExampleMicro2dbEntity(1L, "ExampleMicro2db1", "10"),
            new ExampleMicro2dbEntity(2L, "ExampleMicro2db2", "42")
        );
        when(service.findAll()).thenReturn(ExampleMicro2dbs);
        
        assertEquals(ExampleMicro2dbs, controller.getAll());
    }
    
    @Test
    void getById_ShouldReturnExampleMicro2db() {
        ExampleMicro2dbEntity ExampleMicro2db = new ExampleMicro2dbEntity(1L, "ExampleMicro2db1", "42");
        when(service.findById(1L)).thenReturn(ExampleMicro2db);
        
        assertEquals(ExampleMicro2db, controller.getById(1L));
    }
    
    @Test
    void create_ShouldReturnCreatedExampleMicro2db() {
        ExampleMicro2dbEntity ExampleMicro2db = new ExampleMicro2dbEntity(null, "ExampleMicro2db1", "42");
        ExampleMicro2dbEntity savedExampleMicro2db = new ExampleMicro2dbEntity(1L, "ExampleMicro2db1", "42");
        when(service.save(ExampleMicro2db)).thenReturn(savedExampleMicro2db);
        
        assertEquals(savedExampleMicro2db, controller.create(ExampleMicro2db));
    }
    
    @Test
    void update_ShouldReturnUpdatedExampleMicro2db() {
        ExampleMicro2dbEntity ExampleMicro2db = new ExampleMicro2dbEntity(null, "ExampleMicro2db1", "42");
        ExampleMicro2dbEntity updatedExampleMicro2db = new ExampleMicro2dbEntity(1L, "ExampleMicro2db1", "42");
        when(service.save(ExampleMicro2db)).thenReturn(updatedExampleMicro2db);
        
        assertEquals(updatedExampleMicro2db, controller.update(1L, ExampleMicro2db));
    }
    
    @Test
    void delete_ShouldCallServiceDelete() {
        doNothing().when(service).deleteById(1L);
        controller.delete(1L);
        verify(service).deleteById(1L);
    }
}
