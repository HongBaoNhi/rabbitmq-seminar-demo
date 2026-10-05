package com.seminar.rabbitmq.service;

import com.seminar.rabbitmq.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class Consumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void receiveOrder(String orderDetails) {
        System.out.println("--> Đang xử lý ngầm đơn hàng nhận được từ Queue: " + orderDetails);
    }
}