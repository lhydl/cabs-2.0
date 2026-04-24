# 🚀 CABS 2.0 – Local Setup Guide

Follow this guide step-by-step to set up and run the project locally.

---

# 🧩 1. Backend Setup (Docker / Podman)

## 📌 Prerequisites

Ensure you have the following installed:

* Clone the repository
  https://github.com/lhydl/cabs-2.0.git

* IntelliJ IDEA
  https://www.jetbrains.com/idea/download/?section=windows

* JDK 21 or later

* Podman Desktop (recommended)
  https://podman-desktop.io/downloads

  > During installation, enable **WSL** and install all recommended components (podman-machine,
  etc.)

* (Optional) Docker (can be used instead of Podman)

---

## ⚙️ Import Backend Services

Open the following folders in IntelliJ **as Gradle projects**:

* `api-gateway`
* `core-service`
* `appointment-service`
* `queue-service`

---

## 🛠 Configure Database Initialization

In `core-service`:

* Open `application-docker.properties`
* Ensure:

  ```
  spring.liquibase.enabled=true
  ```

  This will auto-create tables and seed initial data on first run.

---

## 🔨 Build Backend Services

For **each service**:

1. Open the **Gradle panel** (right side in IntelliJ)
2. Navigate to:
   `Tasks → build`
3. Double-click:

   ```
   bootJar
   ```
4. Verify the JAR file is generated at:

   ```
   ./build/libs
   ```

Repeat for all 4 services.

---

## 🐳 Run Containers

From the project root (where `docker-compose.yml` is located):

### Using Podman

```powershell
podman compose up --build
```

### Using Docker

```powershell
docker compose up --build
```

---

## ✅ Verify Setup

You should see **6 running containers**:

* frontend
* api-gateway
* core-service
* appointment-service
* queue-service
* mysql

---

## 🌐 Access Application

* URL: http://localhost:4200
* Login:

  ```
  Username: admin
  Password: admin
  ```

---

## 🧪 Microservice Resilience Test

Stop any service (e.g. `appointment-service` or `queue-service`):

* The application will still function
* Only the affected module will show **Service Unavailable**

---

# ☸️ 2. Backend Setup (Kubernetes with Minikube)

## 📌 Prerequisites

* Complete **Section 1 (Steps 1–3)**

---

## ▶️ Start Environment

```powershell
podman machine start
minikube start --driver=podman
```

---

## 🏗 Build Images

Navigate to the project root folder where the Kubernetes yaml files resides, then run each command:

```powershell
minikube image build -t api-gateway:1.0 ./api-gateway
minikube image build -t core-service:1.0 ./core-service
minikube image build -t appointment-service:1.0 ./appointment-service
minikube image build -t queue-service:1.0 ./queue-service
minikube image build -t frontend:1.0 ./frontend
```

---

## 🚀 Deploy to Kubernetes
At the project root folder, run the following command:
```powershell
kubectl apply -f .
```

---

## 📊 View Running Pods

```powershell
minikube dashboard
```

---

## 🌐 Access Frontend

```powershell
minikube service frontend
```

Browser will open automatically.

---

## 🧪 Microservice Testing (Kubernetes)

### 1. Scale Down a Service

Stop a service:

```powershell
kubectl scale deployment appointment-service --replicas=0
```

Restart it:

```powershell
kubectl scale deployment appointment-service --replicas=1
```

---

### 2. Simulate Failure (Self-Healing)

```powershell
kubectl delete pod <pod-name>
```

Kubernetes will:

* Automatically recreate the pod
* Demonstrate **self-healing capability**

---

### Other useful kubectl command

To restart a service after rebuild:

```powershell
kubectl rollout restart deployment <service-name>
```

To view logs:
```powershell
kubectl logs <pod-name>
```

# 💻 3. Frontend Setup (Local Development)

## 📌 Prerequisites

* Node.js
  https://docs.npmjs.com/downloading-and-installing-node-js-and-npm

* Angular CLI:

```powershell
npm install -g @angular/cli
```

* VS Code (or any preferred editor)

---

## ▶️ Run Frontend

1. Open `frontend` folder in VS Code
2. Run:

```powershell
npm run start
```

---

## 🌐 Access Frontend

* URL: http://localhost:9000
* Login:

  ```
  Username: admin
  Password: admin
  ```

---

# ✅ Summary

| Mode       | URL                   | Notes                     |
|------------|-----------------------|---------------------------|
| Docker     | http://localhost:4200 | Full stack via containers |
| Kubernetes | auto-open             | Uses Minikube service     |
| Local FE   | http://localhost:9000 | Frontend-only dev mode    |

---

# 💡 Tips

* Use Docker setup for **quick testing**
* Use Kubernetes setup for **demoing scalability & resilience**
* Use local frontend for **UI development**

---
