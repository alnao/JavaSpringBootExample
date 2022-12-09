package it.alnao.examples;

import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExampleMicro11asyncConsumerReceiver implements RabbitListenerConfigurer {

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
    }
    
    @Autowired
    ExampleMicro11asyncConsumerService service;
    
    @RabbitListener(queues = "${spring.rabbitmq.queue}" )
    public void receivedMessage(ExampleMicro11asyncConsumerMagazzinoEntity ordine) {
        System.out.println("Order: " + ordine);
        ExampleMicro11asyncConsumerMagazzinoEntity el=new ExampleMicro11asyncConsumerMagazzinoEntity();
        el.setIdOrdine( ordine.getIdOrdine() );
        el.setIdProdotto( ordine.getIdProdotto() );
        el.setQuantita( ordine.getQuantita() );
        service.save(el);
    }
}