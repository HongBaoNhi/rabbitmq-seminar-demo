package com.seminar.rabbitmq.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("kafka")
public class KafkaOrderConsumer {

    @KafkaListener(topics = "orders", groupId = "order-demo-group")
    public void receiveOrder(String orderDetails) {
        System.out.println("--> Processing order from Kafka topic in background: " + orderDetails);
    }
}
