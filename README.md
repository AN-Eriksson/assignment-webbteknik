# UFO Dashboard

Spring Boot + React dashboard for UFO sightings with GitHub OAuth login.

## Tech stack

- Backend: Spring Boot (Java 21)
- Frontend: React + Vite + Leaflet + Supercluster
- Deploy: Docker (single image, backend serves built frontend)

## Run locally

```bash
./gradlew bootRun
```

```bash
cd frontend
npm install
npm run dev
```

## Performance and issue-related implementation

- The app does **not** fetch the whole dataset at once.
- Backend endpoints support pagination (`page`, `size`) for sightings/shapes/locations.
- The map uses **on-demand viewport loading** via `/api/sightings/map` with bounds (`north/south/east/west`).
- Map requests are **debounced** in the frontend to avoid request spam while panning/zooming.
- Large map datasets are **clustered** with Supercluster instead of rendering raw points directly.
- Backend applies a configurable request limit for map responses to keep UI and API responsive.
- Frequently requested API data is cached in-memory with Caffeine.
- UI shows loading and error states for API calls.

## Visualization

- Interactive OpenStreetMap map with zoom/pan, cluster drill-down, and filter support.

## Auth

- OAuth2 login with GitHub (server-side flow via Spring Security).
- Session-based protected API endpoints.

## Docker

```bash
docker compose up --build
```

Default app URL: `http://localhost:3147`

## Environment variables

Create `.env` from `example.env` and set:

- `GITHUB_CLIENT_ID`
- `GITHUB_CLIENT_SECRET`
- `UFO_API_URL`

## Tests

```bash
./gradlew test
```

