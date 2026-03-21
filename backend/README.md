# Getting Started on Local

Please do the following in sequence for initial local setup.
## 1.Setup Database
Prerequisites:  
1. Install podman desktop: https://podman-desktop.io/downloads
(Follow the instructions, use WSL(Windows Subsystem for Linux), install everything(recommended) i.e podman-machine, podman etc)  
You can use Docker as well if preferred.
2. Install MySQL Workbench 8.0 CE: https://dev.mysql.com/downloads/workbench/  
Can use DataGrip if you have JetBrains license.

Setup and run a MySQL 8.0 container with persistent storage:
```powershell
podman run -d `
  --name mysql8 `
  -e MYSQL_ROOT_PASSWORD=Passw0rd12345 `
  -e MYSQL_DATABASE=cabs 
  -p 3306:3306 `
  -v mysql_data:/var/lib/mysql `
  docker.io/mysql:8.0
```

If using Docker, the following is the equivalent command:
```powershell
docker run -d `
  --name mysql8 `
  -e MYSQL_ROOT_PASSWORD=Passw0rd12345 `
  -e MYSQL_DATABASE=cabs `
  -p 3306:3306 \
  -v mysql_data:/var/lib/mysql `
  mysql:8.0
```

Connect to the database using MySQL Workbench 8.0 CE, with the following credentials:
```
Host: localhost
Port: 3306
User: root
Password: Passw0rd12345
```

## 2. Setup Backend

Prerequisites:  
1. Clone the project from https://github.com/lhydl/cabs-2.0.git
2. Install IntelliJ: https://www.jetbrains.com/idea/download/?section=windows
3. Download JDK 26 https://jdk.java.net/26/ (or JDK 21 or later)

Open the project folder `backend` in IntelliJ as a Gradle Project.

Go to `application.properties`, check that `spring.profiles.active=local` to run the local build profile.

Go to `application-local.properties`, check that `spring.liquibase.enabled=true` to set up the database on first run.

Open the file `CabsApp`, click the play button, then modify run config. Ensure the build is using the Java that was just downloaded.

Run the app by clicking the play button, then `Run CabsApp.main()`. If run is successful, you will be able to see `Started CabsApp in .... seconds` in the console.

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

