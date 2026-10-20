package com.seminar.rabbitmq.service;

import com.seminar.rabbitmq.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class Consumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveOrder(String orderDetails) {
        System.out.println("--> Processing order from Queue in background: " + orderDetails);
    }
}