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
    
	@Autowired
	private ExampleMicro11asyncCloudConfig config; //config del Spring Cloud
    //vecchia versione senza Spring Cloud
    //@Value("${spring.rabbitmq.exchange}")
	//private String exchange;
	//@Value("${spring.rabbitmq.routingkey}")
	//private String routingkey;
    
    public void send(ExampleMicro11asyncOrdineEntity ordine){
    	rabbitTemplate.convertAndSend(config.getQueue(), ordine);
        //rabbitTemplate.convertAndSend(routingkey, ordine);
    	System.out.println("Send to queue "+ordine);
    }
    

}
