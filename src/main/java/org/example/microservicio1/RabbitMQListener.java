package org.example.microservicio1;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQListener {

    @RabbitListener(queues = "logQueue")
    public void listen(String message) {
        System.out.println("Received message: " + message);
    }
}