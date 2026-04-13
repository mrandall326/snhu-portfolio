# CS 230 – Operating Platforms  
### Draw It or Lose It: Software Design Document

This repository contains my final project for **CS 230: Operating Platforms** at Southern New Hampshire University.  
The project centers on designing a scalable, secure, and platform‑independent architecture for **Draw It or Lose It**, a game originally developed for Android and now being expanded into a distributed, web‑based application.

The full Software Design Document (SDD) is included in this repository and outlines the system’s requirements, constraints, architecture, domain model, platform evaluation, and final recommendations.

---

## 📌 Project Overview

The Gaming Room requested that their existing Android game be redesigned as a **cross‑platform web application** capable of supporting:

- Multiple teams and players  
- Unique game, team, and player identifiers  
- Real‑time drawing and guessing  
- A single authoritative game instance in memory  
- Consistent behavior across Windows, macOS, Linux, and mobile browsers  

The goal of the project was to analyze operating platforms, evaluate architectural options, and produce a complete software design that supports scalability, security, and distributed operation.

---

## 🧩 Key Components of the Software Design Document

### **1. Executive Summary**
Summarizes the client’s needs and proposes a distributed, web‑based solution using platform‑independent technologies (HTML5, JavaScript, REST APIs).  
The design ensures scalability, secure session management, and consistent performance across devices.

---

### **2. Requirements**

#### **Business Requirements**
- Support multiple teams and players  
- Enforce unique names and identifiers  
- Maintain a single active game instance  
- Follow the established four‑round gameplay format  
- Provide consistent UX across platforms  

#### **Technical Requirements**
- Web‑based distributed architecture  
- Real‑time rendering and synchronization  
- Secure authentication and data protection  
- Platform‑independent implementation  
- Scalability for future growth  

---

### **3. Design Constraints**
The document analyzes constraints such as:

- Concurrency and synchronization  
- Unique identifier enforcement  
- Singleton game instance  
- Distributed environment challenges  
- Latency and rendering performance  
- Security requirements  
- Scalability expectations  

These constraints shape the system’s architecture and technology choices.

---

### **4. Domain Model**
The UML class diagram demonstrates:

- **Inheritance** via a shared `Entity` base class  
- **Composition** between Game → Team → Player  
- **Singleton pattern** for `GameService`  
- **Encapsulation** of attributes and behaviors  
- **Polymorphism** through overridden `toString()` methods  

The model ensures unique identifiers, centralized game management, and a scalable structure for future expansion.

---

### **5. Platform Evaluation**
The SDD includes a detailed comparison of:

- **macOS**
- **Linux**
- **Windows**
- **Mobile devices**

Each platform is evaluated across:

- Server‑side capabilities  
- Client‑side behavior  
- Development tools  
- Licensing considerations  
- Performance and compatibility  
- Security and deployment implications  

This analysis supports selecting the most appropriate hosting environment.

---

### **6. Recommendations**
The final section recommends:

- **Linux** as the primary server platform  
- A **modular, layered architecture** with stateless application instances  
- **PostgreSQL/MySQL** for persistent storage  
- **Redis** or similar for in‑memory state  
- **HTTPS, RBAC, hashing, and container isolation** for security  
- **Cloud‑based load balancing and distributed systems patterns** for scalability and resilience  

These recommendations align with the client’s needs for performance, security, and cross‑platform accessibility.

---

## 🎯 Purpose of This Project

This final project demonstrates my ability to:

- Analyze operating platforms and system architectures  
- Apply object‑oriented design principles  
- Evaluate constraints and platform characteristics  
- Produce a complete, professional software design document  
- Recommend secure, scalable, distributed solutions  

This artifact is part of my long‑term Computer Science portfolio and showcases my skills in system design, platform analysis, and architectural decision‑making.
