# Sync Videos

**Sync Videos** is a microservices-based application developed to showcase my skills in creating scalable and maintainable software solutions. The project demonstrates expertise in Java, Spring Boot, microservices architecture, RESTful API design, and integration with external services.

> **Note:** This project was inspired by [sync-tube.de](https://sync-tube.de/), a platform that allows users to watch videos together in sync.

## 📑 Table of Contents

- [Overview](#-overview)
  - [Microservices Architecture](#microservices-architecture)
  - [Key Features](#key-features)
- [Technical Stack](#-technical-stack)
- [How It Works](#-how-it-works)
- [Future Improvements](#-future-improvements)
- [Limitations](#-limitations)
- [Inspiration](#-inspiration)

## 🛠️ Overview

This project consists of several independent services, each responsible for different aspects of the application. The microservices communicate via RESTful APIs, providing a modular and flexible architecture.

### Microservices Architecture

- **Discovery Service**: Manages service registration and discovery using Netflix Eureka.
- **Gateway Service**: Acts as the entry point for client requests, routing them to the appropriate service.
- **Authentication Service**: Handles user authentication and issues JWT tokens for securing the APIs, integrated with the User Service.
- **Room Service**: Manages video rooms where users can join and watch videos synchronously.
- **User Service**: Manages user-related data, including registration, profiles, and storage.

### Key Features

- **Microservices Architecture**: Ensures scalability and maintainability.
- **Service Discovery**: Dynamic service registration and discovery with Eureka.
- **API Gateway**: Centralized routing and handling of client requests.
- **JWT Authentication**: Secures services with JSON Web Tokens (JWT).
- **Scalability**: Independent scaling of each service based on demand.

## 🧰 Technical Stack

- **Java**: Core programming language.
- **Spring Boot**: Framework for building microservices.
- **Spring Cloud**: Used for service discovery, gateway, and configuration management.
- **Netflix Eureka**: For service discovery and registry.
- **Feign**: Simplifies inter-service communication.
- **MongoDB**: Database for storing users and rooms.
- **JUnit & Mockito**: Testing frameworks for unit and integration tests.

## 🎥 How It Works
Watch the demo video to see the application in action:
[![Watch the video](https://raw.githubusercontent.com/Switix/sync-videos/main/thumbnail.jpg)](https://raw.githubusercontent.com/Switix/sync-videos/main/demo.mp4)

## 🎥 Future Improvements
- Integrate Chat Functionality in Rooms:

  - Add a feature that allows users to send and receive messages within a room.
- Show Existing Rooms for Joining:

  - Implement a feature to list available rooms that users can join. Include options for joining public rooms or entering a password for private rooms.

## ⚠️ Limitations
This project is a demonstration of my skills and is not intended for production use. It focuses on architectural design and code quality, rather than being a fully-featured, production-ready application.

## 🎨 Inspiration
This project was inspired by sync-tube.de, a platform for synchronized video watching. My implementation builds on this idea using a microservices architecture and modern development practices.
