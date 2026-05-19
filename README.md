# Assignment WT - Web for Data Science

## Project Name

UFO Dashboard

## Objective

UFO Dashboard is a web application that visualizes UFO sightings from the WT1 API.
Users sign in with GitHub OAuth and explore sightings on an interactive map.
The app provides filters for country and shape so the dataset can be narrowed down while exploring the map.

## Github repository

URL: https://github.com/AN-Eriksson/assignment-webbteknik 

## Deployed Application

URL: https://ufodashboard.andreaseriksson.me/

## Requirements

See [all requirements in Issues](../../issues/). Close issues as you implement them. Create additional issues for any custom functionality.

### Functional Requirements

| Requirement | Issue | Status |
|---|---|---|
| API Integration — the app consumes your WT1 API | [#14](../../issues/14) | :white_check_mark: |
| OAuth Authentication — users log in via OAuth 2.0 | [#15](../../issues/15) | :white_check_mark: |
| Interactive data visualization with aggregation/adaptation for 10 000+ data points | [#11](../../issues/11) | :white_check_mark: |
| Efficient loading — pagination, lazy loading, loading indicators | [#13](../../issues/13) | :white_check_mark: |

### Non-Functional Requirements

| Requirement | Issue | Status             |
|---|---|--------------------|
| Clear and well-structured code | [#1](../../issues/1) | :white_check_mark: |
| Code reuse | [#2](../../issues/2) | :white_check_mark: |
| Dependency management and scripts | [#3](../../issues/3) | :white_check_mark: |
| Source code documentation | [#4](../../issues/4) | :white_check_mark: |
| Coding standard | [#5](../../issues/5) | :white_check_mark: |
| Examiner can follow the creation process | [#6](../../issues/6) | :white_check_mark: |
| Publicly accessible over the internet | [#7](../../issues/7) | :white_check_mark: |
| Keys and tokens handled correctly | [#8](../../issues/8) | :white_check_mark: |
| Complete assignment report with correct links | [#9](../../issues/9) | :white_check_mark: |

### VG — AI/ML Feature (optional)

For a VG grade, integrate **one** AI/ML feature into the application. Pick one below or propose your own of similar scope. See the [VG issue](../../issues/12) for full details and acceptance criteria.

| Option | Status |
|---|---|
| Semantic Search — natural language queries matched by meaning | :white_large_square: |
| Content-Based Recommendations — "items similar to this one" | :white_large_square: |
| Sentiment Analysis — analyze and visualize text sentiment | :white_large_square: |
| Text Summarization / Generation — LLM-powered summaries | :white_large_square: |
| Clustering & Grouping — auto-group similar items visually | :white_large_square: |
| RAG — natural language Q&A grounded in your dataset | :white_large_square: |
| Other: *describe* | :white_large_square: |

No AI/ML feature is implemented.

## Core Technologies Used

| Layer | Options |
|---|---|
| **Visualization** | Leaflet, Supercluster |
| **Front-end** | React, Vite |
| **Styling** | CSS |


Chosen technologies:
- Leaflet for the interactive map.
- Supercluster for clustering large numbers of map points.
- React and Vite for the frontend.
- Spring Boot for the backend.
- Docker and Docker Compose for deployment.

## How to Use

1. Open the deployed application.
2. Log in with GitHub.
3. Use the country and shape filters to narrow the map data.
4. Use the map controls to pan, zoom, and inspect clustered sightings.
5. Adjust the map limit slider to load more or fewer sightings.

## Acknowledgements

- OpenStreetMap for map tiles.
- GitHub OAuth for authentication.

