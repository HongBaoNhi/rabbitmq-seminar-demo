# RabbitMQ & Spring Boot Integration

This repository contains the source code and setup guide for demonstrating **RabbitMQ** and **Apache Kafka** with Spring Boot.

The demo sends the same order through either broker and processes it asynchronously, making the messaging flows easy to compare.

## Architecture

```text
                     +--> RabbitMQ queue: orderQueue --> RabbitMQ consumer
Browser / Postman --> Spring Boot application
                     +--> Kafka topic: orders --------> Kafka consumer
```

The application declares the durable RabbitMQ queue `orderQueue` in the default mode. When the `kafka` profile is enabled, it also declares the Kafka topic `orders` and activates the Kafka producer and consumer.

## Prerequisites

- **JDK 17 or later**
- **Docker Desktop** on Windows/macOS, or **Docker Engine** on Linux, to run RabbitMQ and Kafka
- **Git**

## 1. Start RabbitMQ

Run the following command to download the RabbitMQ image (if needed) and start the broker container:

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Check that RabbitMQ is running:

```bash
docker ps
```

The app connects to RabbitMQ at `localhost:5672`. Port `15672` is for the management dashboard; port `5672` is for app connections.

> If a container named `rabbitmq` already exists, start it with `docker start rabbitmq` instead of running `docker run` again.

## 2. Clone and run the RabbitMQ demo

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

Wait until Spring Boot reports that the application has started. It uses port `8080` by default. At this stage, the app runs the RabbitMQ demo only.

## 3. Observe the RabbitMQ demo

Visit [http://localhost:15672] and sign in with the default credentials:

```text
Username: guest
Password: guest
```

Open **Queues and Streams** (or **Queues**, depending on the RabbitMQ version) and select `orderQueue`. Then send a test order using a browser or Postman:

```text
http://localhost:8080/send-order?orderDetails=TestOrder123
```

The response is `Order placed successfully: TestOrder123`. The Spring Boot terminal prints:

```text
--> Processing order from Queue in background: TestOrder123
```

Because the consumer handles messages as they arrive, a message may disappear from the queue quickly. Use the consumer output or queue message rates in the dashboard to observe the flow.

## Loop command
```bash
for /L %i in (1,1,1000) do @curl -s "http://localhost:8080/send-order?orderDetails=Order%i"
```

## 4. Start Kafka for comparison

The project already contains the Kafka dependency, connection settings, producer, consumer, and topic configuration. There is no code to copy or add manually.

In another terminal, start a single-node Kafka broker:

```bash
docker run -d --name kafka -p 9092:9092 apache/kafka:4.1.0
```

Check that it is running:

```bash
docker ps
```

The app connects to Kafka at `localhost:9092`.

> If a container named `kafka` already exists, start it with `docker start kafka`.

## 5. Restart the application in comparison mode

Stop the RabbitMQ-only application with `Ctrl+C`. Keep both RabbitMQ and Kafka containers running, then start the app with its `kafka` profile enabled.

### macOS / Linux

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=kafka
```

### Windows (PowerShell)

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=kafka"
```

The `kafka` profile enables the Kafka producer, consumer, and automatic creation of the `orders` topic. The RabbitMQ flow remains available too.

## 6. Compare both brokers

Send the same order to each endpoint in a browser or with Postman:

```text
http://localhost:8080/send-order?orderDetails=TestOrder123
http://localhost:8080/send-order-kafka?orderDetails=TestOrder123
```

The RabbitMQ response is `Order placed successfully: TestOrder123`. The Kafka response is `Order sent to Kafka: TestOrder123`. The Spring Boot terminal prints a consumer line for each broker:

```text
--> Processing order from Queue in background: TestOrder123
--> Processing order from Kafka topic in background: TestOrder123
```



### Observe Kafka records

Port `9092` is Kafka's messaging port, not a website, so opening `http://localhost:9092` in a browser will not show a page. To inspect records, first send an order to the Kafka endpoint above. Then run this command in a **separate terminal**; it prints records in the terminal and keeps listening for new ones:

```bash
docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic orders --from-beginning --group order-observer-1
```

You should see the order value, for example `TestOrder123`. Press `Ctrl+C` to stop the console consumer. Kafka retains records according to the topic's retention settings, even after the application's `order-demo-group` has consumed them. To replay from the beginning again, use a different group name (for example, change `order-observer-1` to `order-observer-2`); each group tracks its own offsets. In contrast, RabbitMQ queue messages are normally removed after the consumer acknowledges them. This is a behavior comparison, not a performance benchmark.

## Troubleshooting

- **Cannot connect to RabbitMQ on port `5672`:** Make sure Docker is running and the `rabbitmq` container is up (`docker ps`). If it is stopped, run `docker start rabbitmq`.
- **Cannot connect to Kafka on port `9092`:** Make sure the `kafka` container is running (`docker ps`). If it is stopped, run `docker start kafka`.
- **Opening `localhost:9092` in a browser does not work:** This is expected; `9092` speaks the Kafka protocol, not HTTP. Use the console-consumer command above to read records in a terminal.
- **The Kafka endpoint returns 404:** Restart the application with the `kafka` profile as shown in Step 6.
- **Docker command cannot connect to the daemon:** Start Docker Desktop (or the Docker Engine service on Linux), then try `docker ps` again.
- **Port `5672` or `15672` is already in use:** Stop or reconfigure the service using that port, then start the container again.
- **Port `9092` is already in use:** Stop or reconfigure the service using that port, then start Kafka again.
- **Port `8080` is already in use:** Stop the other application using port `8080`, or configure this application's server port before running it.
- **The RabbitMQ dashboard does not open:** Confirm the RabbitMQ container is running and that port `15672` was published.

## Summary

The application demonstrates asynchronous producer-consumer messaging with both RabbitMQ and Kafka. Use the same order value for each endpoint to compare queue acknowledgements with Kafka topics, retained records, and consumer offsets.
