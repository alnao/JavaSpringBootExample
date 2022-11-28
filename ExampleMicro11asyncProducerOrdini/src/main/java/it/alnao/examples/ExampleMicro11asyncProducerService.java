package it.alnao.examples;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ExampleMicro11asyncProducerService {
	
	@Autowired
    private RabbitTemplate rabbitTemplate;
    
    
    //@Autowired
    //public ExampleMicro11asyncProducerService(RabbitTemplate rabbitTemplate) {
    //    this.rabbitTemplate = rabbitTemplate;
    //}
    
    @Value("${spring.rabbitmq.exchange}")
    private String exchange;
    @Value("${spring.rabbitmq.routingkey}")
    private String routingkey;
    
    public void send(ExampleMicro11asyncOrdineEntity user){
    	rabbitTemplate.convertAndSend(routingkey, user);
        //rabbitTemplate.convertAndSend(exchange,routingkey, user);
    }
}
