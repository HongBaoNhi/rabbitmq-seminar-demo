# RabbitMQ & Spring Boot Integration

This repository contains the source code and setup guide for our seminar on **Data & Infrastructure Services: RabbitMQ**.

The demo shows how a Spring Boot application can send an order message to RabbitMQ and process it asynchronously with a consumer.

## Architecture

```text
Browser / Postman
       |
       | GET /send-order?orderDetails=...
       v
Spring Boot Producer
       |
       | publishes message
       v
RabbitMQ queue: orderQueue
       |
       | delivers message
       v
Spring Boot Consumer
```

The application declares the durable `orderQueue` queue at startup. The producer publishes messages to it, and the consumer listens for and processes them asynchronously.

## Prerequisites

- **JDK 17 or later**
- **Docker Desktop** (or Docker Engine) to run RabbitMQ
- **Git**

## 1. Start RabbitMQ

Start Docker, then run this command in a terminal:

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Check that the container is running:

```bash
docker ps
```

The application connects to RabbitMQ at `localhost:5672`. The management dashboard is available at `http://localhost:15672`.

> If a container named `rabbitmq` already exists, start it with `docker start rabbitmq` instead of running `docker run` again.

## 2. Clone and run the application

Clone the repository and change into its directory:

```bash
git clone https://github.com/HongBaoNhi/rabbitmq-seminar-demo.git
cd rabbitmq-seminar-demo
```

Run the Spring Boot application from the project directory.

### macOS / Linux

```bash
./mvnw spring-boot:run
```

### Windows (PowerShell)

```powershell
.\mvnw.cmd spring-boot:run
```

Wait until Spring Boot reports that the application has started. It uses port `8080` by default.

## 3. Open the RabbitMQ dashboard

Visit [http://localhost:15672](http://localhost:15672) and sign in with the default credentials:

```text
Username: guest
Password: guest
```

Open **Queues and Streams** (or **Queues**, depending on the RabbitMQ version) and select `orderQueue`.

## 4. Send a test order

With the application running, open this URL in a browser or send a GET request with Postman:

```text
http://localhost:8080/send-order?orderDetails=TestOrder123
```

The HTTP response from the producer is:

```text
Order placed successfully: TestOrder123
```

The consumer processes the message asynchronously and prints a line like this in the Spring Boot terminal:

```text
--> Processing order from Queue in background: TestOrder123
```

Because the consumer handles messages as they arrive, a message may disappear from the queue quickly. Use the consumer output in the application terminal, or the queue's message rates in the dashboard, to observe the flow.

## 5. Add Kafka for comparison (optional)

The current application only contains the RabbitMQ demo. Follow this section to add a Kafka producer and consumer alongside it, so both brokers can be tested with the same order value.

### Start a local Kafka broker

In another terminal, start a single-node Kafka broker with Docker:

```bash
docker run -d --name kafka -p 9092:9092 apache/kafka:4.1.0
```

Check that it is running:

```bash
docker ps
```

> If a container named `kafka` already exists, start it with `docker start kafka`.

### Add the Kafka dependency

In `pom.xml`, add this dependency inside `<dependencies>`:

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

The Spring Boot parent manages the dependency version. Save the file and let Maven reload the project.

### Configure Kafka

Append these settings to `src/main/resources/application.properties`:

```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=order-demo-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

### Create the Kafka topic

Create `src/main/java/com/seminar/rabbitmq/config/KafkaConfig.java`:

```java
package com.seminar.rabbitmq.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic ordersTopic() {
        return TopicBuilder.name("orders")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
```

The single replica is appropriate for this local, single-broker demo; it is not a production availability setup.

### Add a Kafka endpoint and consumer

Create `src/main/java/com/seminar/rabbitmq/service/KafkaOrderProducer.java`:

```java
package com.seminar.rabbitmq.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
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
```

Create `src/main/java/com/seminar/rabbitmq/service/KafkaOrderConsumer.java`:

```java
package com.seminar.rabbitmq.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaOrderConsumer {

    @KafkaListener(topics = "orders", groupId = "order-demo-group")
    public void receiveOrder(String orderDetails) {
        System.out.println("--> Processing order from Kafka topic in background: " + orderDetails);
    }
}
```

### Run and compare both flows

Keep RabbitMQ and Kafka running, then start the Spring Boot application as described above. Send the same order to each endpoint:

```text
http://localhost:8080/send-order?orderDetails=TestOrder123
http://localhost:8080/send-order-kafka?orderDetails=TestOrder123
```

The RabbitMQ endpoint responds with `Order placed successfully: TestOrder123`. The Kafka endpoint responds with `Order sent to Kafka: TestOrder123`. The application terminal prints a separate consumer log for each broker.

To inspect Kafka records after the application has consumed them, use a **new consumer group** in another terminal:

```bash
docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic orders --from-beginning --group order-observer
```

This observer group reads the retained topic records independently of the application's `order-demo-group`. Kafka records are retained according to the topic's retention settings; the application's consumer tracks its progress with offsets. In contrast, RabbitMQ queue messages are normally removed after the consumer acknowledges them. This is a behavior comparison, not a performance benchmark.

## Troubleshooting

- **Cannot connect to RabbitMQ on port `5672`:** Make sure Docker is running and the `rabbitmq` container is up (`docker ps`). If it is stopped, run `docker start rabbitmq`.
- **Cannot connect to Kafka on port `9092`:** Make sure the `kafka` container is running (`docker ps`). If it is stopped, run `docker start kafka`.
- **Port `5672` or `15672` is already in use:** Stop or reconfigure the service using that port, then start the container again.
- **Port `9092` is already in use:** Stop or reconfigure the service using that port, then start Kafka again.
- **Port `8080` is already in use:** Stop the other application using port `8080`, or configure this application's server port before running it.
- **The RabbitMQ dashboard does not open:** Confirm the RabbitMQ container is running and that port `15672` was published.

## Summary

The existing application demonstrates asynchronous producer-consumer messaging with RabbitMQ. The optional Kafka walkthrough adds a second broker to compare queue acknowledgements with Kafka topics, retained records, and consumer offsets using the same order example.
