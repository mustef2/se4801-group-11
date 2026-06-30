<div align="center">

<img src="https://capsule-render.vercel.app/api?type=waving&color=2ecc71&height=200&section=header&text=🌱%20EcoTrack%20Backend&fontSize=40&fontColor=ffffff&animation=fadeIn&fontAlignY=35&desc=Spring%20Boot%20Sustainability%20API&descAlignY=55&descSize=18" width="100%"/>

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)
![Version](https://img.shields.io/badge/Version-0.0.1--SNAPSHOT-orange?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-In%20Development-yellow?style=for-the-badge)

</div>

---

## 📋 Table of Contents
- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Project Structure](#-project-structure)
- [Environment Variables](#-environment-variables)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🌍 Overview

**EcoTrack Backend** is a Spring Boot REST API for the Sustainability Tracker system. It handles data collection, validation, user management, and analytics for energy, water, waste, emissions, and governance metrics.

---

## ✨ Features

- Modular Data Entry (Energy, Water, Waste, Emissions, Social & Governance)
- Department & Company Level Tracking
- JWT Authentication & Role-based Authorization
- Data Validation with Jakarta Bean Validation
- Approval Workflow Support
- Historical Data & Advanced Filtering
- Docker-ready deployment

---

## 🛠 Tech Stack
| Component       | Technology                    | Version      |
|-----------------|-------------------------------|--------------|
| Framework       | Spring Boot                   | 3.3.4        |
| Language        | Java                          | 21           |
| Database        | MySQL + Flyway                | 8.4          |
| ORM             | Spring Data JPA               | 3.3.4        |
| Security        | Spring Security + JWT         | 6.4.0        |
| Validation      | Jakarta Validation + Custom   | 3.1.0        |
| Build Tool      | Maven                         | 3.9.9        |
| Container       | Docker + Compose              | Latest       |

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Maven 3.9+
- MySQL 8 or Docker

### Local Development

```bash
# Clone the repository
git clone https://github.com/mustef2/se4801-group-11
cd ecotrack-backend

# Copy environment file
cp .env.example .env

# Run with Maven (Development)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or run with Docker
docker-compose up --build -d
Access: API Base URL → http://localhost:8080
```

## 📡 API Documentation

**Base URL**: `/`

**Main Endpoints:**

- `POST /auth/login`
- `POST /auth/register`
- `POST /energy` → Submit Energy Data
- `GET /energy` → Get Energy Records
- Similar routes for Water, Waste, Emission, Social, Governance, etc.

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -m 'feat: add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Open a Pull Request

---

## 📄 License

MIT License © 2025

---

<div align="center">
Built with ❤️ for a Greener Future 🌍
</div>