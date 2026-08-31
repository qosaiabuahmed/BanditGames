# BanditGames

BanditGames is a full-stack board game platform built as a group project during my Applied Computer Science studies at KdG.

The idea behind the project was to build a platform where different board games can be integrated and played through one central application, while also supporting AI opponents, authentication, matchmaking and real-time game communication.

The project was split into multiple services and applications that communicate with each other rather than being built as one monolithic application.

## Architecture

At a high level, BanditGames consists of:

- A React/TypeScript frontend for the main platform
- A Spring Boot backend responsible for the core platform and business logic
- PostgreSQL for persistence
- Keycloak for authentication and authorization
- RabbitMQ for event-driven communication between services
- Separate game services and frontends
- Python-based services for game logic and AI
- A chatbot service
- WebSockets for real-time communication
- Docker/Docker Compose for running the different services together

The individual services can be found in the different folders in this repository.

## Main Components

### `react-frontend`
The main React frontend of the BanditGames platform. It handles the user-facing side of the application, authentication, game discovery, matchmaking, user profiles, social features and communication with the backend.

### `spring-backend`
The central Spring Boot backend of the platform.

It handles the main application domain and acts as the central point between the frontend and the different services. It also contains the Keycloak integration, RabbitMQ messaging configuration, WebSocket communication and the integration layer for external games.

### `game-backend`
A separate Python-based game service containing game logic, APIs, persistence and AI-related functionality.

### `ai-implementation`
Contains the AI implementation used for the board games.

### Game integrations

The platform was designed so that games could exist as separate applications while still integrating with the central BanditGames platform.

The repository includes:

- `tic-tac-toe`
- `tic-tac-toe-frontend`
- `connectfour-frontend`

### Chatbot

The chatbot is separated into:

- `chatbot`
- `chatbot-frontend`

## Event-Driven Communication

One of the main architectural parts of the project is the communication between the central platform and the individual game services.

RabbitMQ is used for event-driven communication between services. The platform defines exchanges, queues and routing keys for events such as:

- game registration
- player actions
- match lifecycle events
- lobby events
- achievements
- user events

This allowed the individual services to remain separated while still reacting to events happening elsewhere in the platform.

## Authentication

Authentication and authorization are handled through Keycloak using JWTs.

The React application authenticates users through Keycloak and sends access tokens when communicating with protected backend endpoints. The Spring Boot backend acts as an OAuth2 resource server and validates these tokens.

Authentication is also taken into account for WebSocket connections and when users are redirected from the main platform to individual games.

## Infrastructure

The different parts of the application were containerized with Docker.

Docker Compose is used to bring together the application services and supporting infrastructure such as:

- PostgreSQL
- Keycloak
- RabbitMQ
- Spring Boot services
- Frontend applications
- Game services

This made it possible to run a system made up of several independently developed components in one environment.

## Tech Stack

**Frontend**
- React
- TypeScript
- Vite
- Tailwind CSS

**Backend**
- Java
- Spring Boot
- Python

**Data & Communication**
- PostgreSQL
- RabbitMQ
- WebSockets

**Authentication**
- Keycloak
- OAuth2 / JWT

**Infrastructure**
- Docker
- Docker Compose

**AI / ML**
- Python
- MCTS
- MLflow
- DVC

## Repository Structure

```text
BanditGames/
├── react-frontend/
├── spring-backend/
├── game-backend/
├── ai-implementation/
├── chatbot/
├── chatbot-frontend/
├── tic-tac-toe/
├── tic-tac-toe-frontend/
└── connectfour-frontend/
