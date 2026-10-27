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

## Troubleshooting

- **Cannot connect to RabbitMQ on port `5672`:** Make sure Docker is running and the `rabbitmq` container is up (`docker ps`). If it is stopped, run `docker start rabbitmq`.
- **Port `5672` or `15672` is already in use:** Stop or reconfigure the service using that port, then start the container again.
- **Port `8080` is already in use:** Stop the other application using port `8080`, or configure this application's server port before running it.
- **The dashboard does not open:** Confirm the RabbitMQ container is running and that port `15672` was published.

## Summary

This demo integrates RabbitMQ with Spring Boot to illustrate asynchronous communication using the producer-consumer pattern. The producer and consumer communicate through `orderQueue` rather than calling each other directly.
