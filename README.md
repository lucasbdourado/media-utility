# Media Utility

Minimal Spring Boot and React scaffold for the Media Utility MVP.

## Build

```bash
./mvnw package
```

On Windows PowerShell:

```powershell
.\mvnw.cmd package
```

The Maven package build runs the frontend npm install/build workflow and copies `frontend/dist` into the Spring Boot static assets.

## Frontend Development

```bash
cd frontend
npm install
npm run dev
```

## Backend Development

```bash
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```
