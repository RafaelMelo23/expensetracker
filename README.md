
# 💸 Expense Tracker

Este é um sistema de controle de despesas pessoais desenvolvido com **Java 21** e **Spring Boot 3.4.5**, utilizando **PostgreSQL** como banco de dados e **JWT** para autenticação.  
O projeto também conta com **monitoramento via Prometheus e Grafana**, além de uma **interface web construída com: HTML, CSS, Javascript & Thymeleaf**.

---

## 🔧 Tecnologias Utilizadas

- ✅ Java 21
- ✅ Spring Boot 3.4.5
- ✅ Spring Security com JWT + Bcrypt
- ✅ PostgreSQL
- ✅ Thymeleaf (HTML)
- ✅ Docker + Docker Compose
- ✅ Prometheus + Grafana
- ✅ Maven
- ✅ Thymeleaf

---

## 📦 Funcionalidades

- Cadastro e login de usuários com autenticação via JWT
- Criação, edição e exclusão de despesas
- Categorias de despesas
- Listagem de todas as despesas por usuário
- Interface web com Thymeleaf
- Monitoramento de métricas com Prometheus
- Dashboard com Grafana

---
## 📸 Capturas de Tela

### Primeiro Login condicional com Thymeleaf:
![Dashboard de Despesas](./screenshots/first-login.png)

### Dashboard de despesas:
![Dashboard de Despesas](./screenshots/main-page.png)

### Overview Anual:
![Dashboard de Despesas](./screenshots/calendar.png)

---


## 🚀 Como executar o projeto

É necessário ter o **Maven,** **Docker** e **Docker Compose** instalados.

### 1. Clone o repositório:

```bash
 git clone https://github.com/RafaelMelo23/expensetracker.git
cd expense-tracker
```

### 2. Build:

```bash
 mvn clean package
```

### 3. Inicie os containers:

```bash
 docker-compose up --build
```

### 4. Acesse a aplicação:

- Web App: [http://localhost:8080](http://localhost:8080)
- Grafana: [http://localhost:3000](http://localhost:3000)  
  *(usuário padrão: `admin` / senha: `admin`)*

---

## ⚙️ Endpoints da API (Simplificado)

- `POST /auth/register`: cadastro de novo usuário
- `POST /auth/login`: autenticação (retorna JWT)
- `GET /expenses`: lista de despesas do usuário autenticado
- `POST /expenses`: criar nova despesa
- `PUT /expenses/{id}`: atualizar despesa
- `DELETE /expenses/{id}`: deletar despesa

---

## 📊 Monitoramento

O projeto expõe métricas no endpoint /actuator/prometheus, que são consumidas pelo Prometheus, que é protegido pela autenticação JWT, periodicamente substituindo seu próprio token.Você pode acompanhar o uso do sistema via dashboards no Grafana.

---

## 📦 API REST - Controllers

A aplicação segue uma arquitetura RESTful e está dividida em controllers responsáveis por diferentes domínios do sistema:

### 🔐 `UserController` (`/api/user`)
Gerencia autenticação e dados financeiros do usuário:

- `POST /register`: Registro de novos usuários
- `POST /login`: Autenticação com JWT via cookie `HttpOnly`
- `GET /get/balance`: Retorna o saldo atual do usuário
- `GET /get/salary`: Retorna o valor do salário atual
- `GET /get/salary/spent`: Retorna a porcentagem do salário já utilizada

### 💰 `AdditionsController` (`/api/additions`)
Responsável por operações relacionadas a adições de valores:

- `GET /get/yearly?year=2024`: Lista todas as adições do ano informado
- `POST /add/balance`: Adiciona um valor ao saldo atual
- `PUT /salary/update`: Atualiza o valor do salário
- `PUT /salary/date/update`: Atualiza o dia do mês em que o salário é recebido

### 🧾 `ExpenseController` (`/api/expense`)
Gerencia o registro e listagem de despesas:

- `POST /first/registry`: Registro inicial de despesas após o cadastro
- `POST /register`: Adiciona uma nova despesa
- `GET /get/all`: Lista todas as despesas
- `GET /get/all/v2`: Lista despesas agrupadas por mês  