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

