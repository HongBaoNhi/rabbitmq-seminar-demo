package com.seminar.rabbitmq;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		classes = RabbitmqApplication.class,
		properties = {
				"spring.rabbitmq.listener.simple.auto-startup=false",
				"spring.kafka.admin.auto-create=false",
				"spring.kafka.listener.auto-startup=false"
		})
class RabbitmqApplicationTests {

	@Test
	void contextLoads() {
	}

}
