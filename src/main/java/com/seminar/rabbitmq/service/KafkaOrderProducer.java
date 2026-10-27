package com.seminar.rabbitmq.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("kafka")
public class KafkaOrderProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaOrderProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @GetMapping("/send-order-kafka")
    public String sendOrder(@RequestParam String orderDetails) {
        kafkaTemplate.send("orders", orderDetails).join();
        return "Order sent to Kafka: " + orderDetails;
    }
}
