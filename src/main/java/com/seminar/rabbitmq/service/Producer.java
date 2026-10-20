package com.seminar.rabbitmq.service;

import com.seminar.rabbitmq.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Producer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/send-order")
    public String sendOrder(@RequestParam String orderDetails) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, orderDetails);
        return "Order placed successfully: " + orderDetails;
    }
}