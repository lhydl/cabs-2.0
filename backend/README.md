# Get Started on Local
## 1.Setup Database

Install podman desktop
https://podman-desktop.io/downloads
(Follow the instructions, use WSL(Windows Subsystem for Linux), install everything(recommended) i.e podman-machine, podman etc)

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

If using Docker and not Podman, the following is the equivalent Docker command:
```powershell
docker run -d `
  --name mysql8 `
  -e MYSQL_ROOT_PASSWORD=Passw0rd12345 `
  -e MYSQL_DATABASE=cabs `
  -p 3306:3306 \
  -v mysql_data:/var/lib/mysql `
  mysql:8.0
```

## 1. Setup Frontend
Open the project folder `frontend` in VS Code.

Run the following command in powershell
```powershell
npm run start
```

## 1. Setup Backend
Open the project folder `backend` in IntelliJ


