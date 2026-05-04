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

