# Getting Started

Please do the following in sequence for initial local setup.

## 1.Setup Backend (Microservices Containers)

Prerequisites:

1. Clone the project from https://github.com/lhydl/cabs-2.0.git
2. Install IntelliJ: https://www.jetbrains.com/idea/download/?section=windows
3. Download and install JDK 21 or later.
4. Install podman desktop: https://podman-desktop.io/downloads
   (Follow the instructions, use WSL(Windows Subsystem for Linux), install everything(recommended)
   i.e podman-machine, podman etc).
   You can use Docker as well if preferred.

Open the backend project folders `api-gateway`, `core-service`, `appointment-service`, and
`queue-service` in IntelliJ as a
Gradle Project.

For `core-service` application: Go to `application-docker.properties`, check that `spring.liquibase.enabled=true` to create
tables and static data for the database on first run.

Build all backend services:

1. Click on the Gradle icon on the right of your IntelliJ IDE,
   navigate to Tasks -> build, then double click on `bootJar` to build the application as a JAR
   file.
2. To verify if the build is successful, you can find the built JAR file in directory
   `./build/libs`.
3. Repeat the above steps for all 4 backend services.

Open terminal on the project root folder where the `docker-compose.yml` file resides, and run the following command to spin up the containers:

```powershell
podman compose up --build
```

or if using docker

```powershell
docker compose up --build
```

## 3. Setup Frontend

Prerequisites:

1. Install Node.js: https://docs.npmjs.com/downloading-and-installing-node-js-and-npm
2. Install Angular CLI (recommended) using the following command after step 1:

```powershell
npm install -g @angular/cli
```

3. Install VS Code or other preferred editor for frontend.

Open the project folder `frontend` in VS Code.

Run the following command in the project folder using powershell:

```powershell
npm run start
```

Navigate to http://localhost:9000/ in your browser to access the app's frontend.

Login to the app with the following credentials:

```
Username: admin
Password: admin
```
