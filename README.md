# UFO Dashboard

Minimal Spring Boot + React dashboard for UFO sightings.

## Run locally

Backend:

```bash
./gradlew bootRun
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

## Visualizations

- Pie chart for shape share
- Line chart for sightings over time
- OpenStreetMap pin map for sightings with coordinates

## Performance notes

- The list uses pagination.
- The charts use a bounded sample of sightings.
- The map loads sightings for the visible viewport and clusters nearby pins.

## Testing

`src/test/resources/application.properties` provides dummy OAuth and API values so `./gradlew test` can run without local secrets.

# Assignment Webbteknik

## Overview

This project has:
- a Spring Boot backend with GitHub OAuth2
- a React frontend in `frontend/`
- a single production Docker image where the backend serves the built frontend files

## Local development

Run the backend:

```bash
./gradlew bootRun
```

Run the frontend separately for development:

```bash
cd frontend
npm install
npm run dev
```

## Docker deployment

The Docker setup builds the React app and packages it into the backend image.
Only one container is needed in production.

Build and run with Compose:

```bash
docker compose up --build
```

Backend + frontend are then available at:
- http://localhost:8080

## Environment variables

Create a `.env` file from `.env.example` and fill in:
- `GITHUB_CLIENT_ID`
- `GITHUB_CLIENT_SECRET`
- `UFO_API_URL`

