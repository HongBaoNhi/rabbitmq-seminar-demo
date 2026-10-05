# RabbitMQ & Spring Boot Integration

Hello everyone!

This repository contains the full source code and a step-by-step setup guide for our seminar on **Data & Infrastructure Services: RabbitMQ**.

In this project, we demonstrate how to use **RabbitMQ** to build an asynchronous and decoupled message queue system using the **Producer-Consumer pattern** in a Spring Boot application.

Feel free to clone, run, and adapt this code for your own semester projects!

---

## Prerequisites

Before running the project, make sure you have installed:

1. **Java Development Kit (JDK 17 or 21)**
2. **Docker Desktop** — Required to run the RabbitMQ broker
3. **Git**

---

# Step-by-Step Setup Guide

## Step 1: Start RabbitMQ with Docker 🐳

Open your terminal (**Terminal** on macOS / **PowerShell** or **Command Prompt** on Windows) and run:

```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### Verify RabbitMQ

**1. Check the container status**

Run:

```bash
docker ps
```

Make sure the RabbitMQ container appears with the status:

```text
Up
```

**2. Open the RabbitMQ Management Dashboard**

Open your browser and go to:

```text
http://localhost:15672
```

**3. Log in with the default credentials**

```text
Username: guest
Password: guest
```

---

## Step 2: Clone & Run the Spring Boot Application 💻

### 1. Clone the repository

```bash
git clone https://github.com/HongBaoNhi/rabbitmq-seminar-demo.git
cd rabbitmq-seminar-demo
```

### 2. Run the application

#### On macOS / Linux

Open Terminal in the project root folder and execute:

```bash
./mvnw spring-boot:run
```

#### On Windows

Open CMD or PowerShell in the project root folder and execute:

```cmd
mvnw.cmd spring-boot:run
```

> **Note:** The application will automatically connect to RabbitMQ at `localhost:5672` and start the embedded Tomcat server on port `8080`.

---

## Step 3: Test the Producer-Consumer Flow

Once the Spring Boot application is running, open your browser or Postman and trigger the REST API:

```text
http://localhost:8080/send-order?orderDetails=TestOrder123
```

### Expected Results

#### 1. Browser Output — Producer Response

```text
Đã gửi đơn hàng thành công: TestOrder123
```

#### 2. Terminal Console — Consumer Output

The consumer will process the message asynchronously in real time.

```text
--> Đang xử lý ngầm đơn hàng nhận được từ Queue: TestOrder123
```

#### 3. RabbitMQ Dashboard

Open:

```text
http://localhost:15672
```

Then go to the **Queues** tab to observe the message activity in real time.

---

# Common Troubleshooting & Fixes

## Issue 1: `Port 8080 was already in use`

### macOS

Run the following command in Terminal to free port `8080`:

```bash
kill -9 $(lsof -t -i:8080)
```

### Windows

First, find the PID using port `8080`:

```cmd
netstat -ano | findstr :8080
```

Then terminate the process:

```cmd
taskkill /PID <PID_NUMBER> /F
```

---

## Issue 2: `Connection Refused` on port `5672`

Make sure **Docker Desktop** is running and the RabbitMQ container is active.

Check the container with:

```bash
docker ps
```

If the RabbitMQ container has stopped, restart it using:

```bash
docker start rabbitmq
```

---

#Project Architecture

```text
src/main/java/com/seminar/rabbitmq/
├── config/
│   └── RabbitMQConfig.java
│       # Queue & Exchange configuration
│
├── service/
│   ├── Producer.java
│   │   # REST Controller sending messages to the Queue
│   │
│   └── Consumer.java
│       # Service listening to the Queue and processing tasks
│
└── RabbitmqApplication.java
    # Spring Boot Main Class
```

---

## Project Flow

```text
Client
   │
   │ HTTP Request
   ▼
Producer
   │
   │ Message
   ▼
RabbitMQ Queue
   │
   │ Asynchronous Delivery
   ▼
Consumer
   │
   ▼
Process Order
```

---

## Summary

This project demonstrates how **RabbitMQ** can be integrated with **Spring Boot** to implement asynchronous communication between a **Producer** and a **Consumer**.

The architecture helps separate message production from message processing, making the system more decoupled and suitable for distributed applications.
