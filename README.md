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

## Running with Docker

### Build the Image

To build the Docker image locally, run the following command from the root of the repository:

```bash
docker build -t media-utility .
```

### Run the Container

To run the application inside a container, you need to configure the required environment variables. For example, to run the container connecting to a local MySQL instance (ensure the database exists):

```bash
docker run -p 8080:8080 \
  -e MEDIA_UTILITY_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/media_utility \
  -e MEDIA_UTILITY_DATASOURCE_USERNAME=root \
  -e MEDIA_UTILITY_DATASOURCE_PASSWORD=root \
  -e MEDIA_UTILITY_STORAGE_ROOT=/app/storage \
  -v media-utility-storage:/app/storage \
  media-utility
```

> [!NOTE]
> `host.docker.internal` allows the container to connect to a database service running on the host machine. Adjust the JDBC connection string and credentials to match your setup.

### Verifying Tool Availability

You can verify that the external tools (`ffmpeg` and `yt-dlp`) are successfully packaged and accessible inside the built image:

```bash
# Check ffmpeg version
docker run --rm --entrypoint ffmpeg media-utility -version

# Check yt-dlp version
docker run --rm --entrypoint yt-dlp media-utility --version
```

## Running with Docker Compose (Recommended for Local Development)

Docker Compose coordinates the Spring Boot application and a MySQL 8.0 database in a single command, making local development setup straightforward.

### 1. Create Your Local Environment File

Copy the provided template and adjust the values if needed:

```bash
cp .env.compose .env
```

The `.env` file is git-ignored. The template `.env.compose` is tracked and contains safe defaults for local development.

### 2. Start the Stack

Build and start both containers in the background:

```bash
docker compose up --build -d
```

The application container will wait for MySQL to pass its health check before starting.

### 3. View Application Logs

```bash
docker compose logs -f app
```

Look for successful JPA connection and schema update messages to confirm the application booted correctly.

### 4. Access the Application

Open [http://localhost:8080](http://localhost:8080) in your browser (or the port configured as `APP_HOST_PORT` in your `.env` file).

### 5. Stop and Teardown

Stop containers but keep the database volume:

```bash
docker compose down
```

Stop containers and delete the database volume (full reset):

```bash
docker compose down -v
```

### Customizing Host Ports

If ports `8080` or `3306` are already in use on your machine, edit your `.env` file:

```dotenv
APP_HOST_PORT=9090
MYSQL_HOST_PORT=3307
```

### Linux Host: Storage Directory Permissions

The application container runs as `appuser` (UID `10001`). On Linux hosts, the host-mounted `./storage` directory must be writable by this UID. If you encounter `AccessDeniedException`, create the directory and set ownership:

```bash
mkdir -p storage
sudo chown -R 10001:10001 storage
```
