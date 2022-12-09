package it.alnao.examples;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ExampleMicro11asyncConsumerService  {

	@Autowired
	ExampleMicro11asyncConsumerRepository repository;
	
	public List<ExampleMicro11asyncConsumerMagazzinoEntity> findAll(){
		return repository.findAll();
	}
	public ExampleMicro11asyncConsumerMagazzinoEntity save(ExampleMicro11asyncConsumerMagazzinoEntity el){
		return repository.save(el);
	}
	
}