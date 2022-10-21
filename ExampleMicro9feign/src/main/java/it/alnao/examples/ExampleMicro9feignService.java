package it.alnao.examples;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ExampleMicro9feignService {
    @Autowired
    ExampleMicro9feignRepository repository;
    @Autowired
    ExampleMicro9feignMagazzinoInterface exampleMicro9feignMagazzinoInterface;
    
    public List<ExampleMicro9feignEntityWithMagazzino> findAll(String authHeader){
        List<ExampleMicro9feignEntity> l=repository.findAll();
        List<ExampleMicro9feignEntityWithMagazzino> lm=new ArrayList<ExampleMicro9feignEntityWithMagazzino>();
        l.forEach( el -> lm.add(new ExampleMicro9feignEntityWithMagazzino(el) ) );
        lm.forEach( el -> el.setMagazzino( 
                exampleMicro9feignMagazzinoInterface
                .magazzino( authHeader , el.getNome()).getBody().get(0) ) ); 
        return lm;
    }


    public ExampleMicro9feignEntity save(ExampleMicro9feignEntity el){
        return repository.save(el);
    }


}