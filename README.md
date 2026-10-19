## Coding Assessment Tasks

### Intro
This is a URL shortener that has been created by a junior engineer. Your task is to review the solution, check that it works, and find any errors they may have made, as well as complete the fixes and TODOs listed below.

This solution has been pre-seeded with data in the database already.

Note that these are not necessarily in the order you will need to address them.

### Fixes

- **Fix Delete Behavior:** The client has reported strange behavior when clicking on delete on a URL. Investigate and fix the issue.
- **Generate Alias:** Implement the `generateAlias` function.
- **UI Implementation:** Build out the UI to display the URLs on the main page.

### TODO

- Add paging for retrieval of the URLs.
- Sort URLs by their creation date.
- Create the Docker commands to run the solution.
- Replace this README with an appropriate one.

---

# Snip – URL Shortener

A full-stack URL shortener built with **Java Spring Boot** (backend) and **React + TypeScript** (frontend), containerized with Docker.

---

## Architecture

```
frontend/      React + TypeScript (Vite)
backend/       Java Spring Boot Web API + SQLite
docker-compose.yml       Orchestrates both services
```

The frontend proxies API calls through Nginx in production, so there are no CORS concerns once containerized. In development, Vite's dev server proxies to the local API.

---

## API Reference

The API exposes the same contract expected by the existing frontend.

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/shorten` | Create a shortened URL |
| `GET` | `/urls` | List all shortened URLs |
| `GET` | `/{alias}` | Redirect to the full URL |
| `DELETE` | `/{alias}` | Delete a shortened URL |

### POST /shorten

```json
// Request
{
  "fullUrl": "https://example.com/very/long/path",
  "customAlias": "my-alias"   // optional
}

// Response 201
{
  "alias": "my-alias",
  "fullUrl": "https://example.com/very/long/path",
  "shortUrl": "http://localhost:8080/my-alias"
}
```

Returns `400` if the URL is invalid, the alias is malformed, or the alias is already taken.

### GET /urls

```json
// Response 200
[
  {
    "alias": "my-alias",
    "fullUrl": "https://example.com/very/long/path",
    "shortUrl": "http://localhost:8080/my-alias"
  }
]
```

### GET /{alias}

Returns `302 Redirect` to the full URL, or `404` if the alias doesn't exist.

### DELETE /{alias}

Returns `204 No Content` on success, `404` if alias not found.

---

## Design Decisions & Assumptions

**Persistence** - SQLite via JDBC. The database file path is configurable via the `DATABASE_PATH` environment variable and is volume-mounted in Docker so data survives container restarts.

**Alias validation** - Aliases must be 2-64 characters, containing only letters, numbers, and hyphens. This mirrors typical URL shortener conventions and avoids characters that are awkward in URLs.

**Random alias generation** - 

**Error handling** - Invalid input and domain errors are translated into JSON error responses like `{ "error": "..." }`.

**Testing** - The backend includes service-level tests and can be extended with integration tests. The frontend includes React component tests and service-level API tests.

**Frontend** - The hosted frontend uses Nginx to proxy `/shorten`, `/urls`, and alias requests to the API, so the same Docker setup works without CORS issues.

## Docker Commands

Run the project command wrapper from the repository root. Windows users can choose PowerShell or Command Prompt; macOS/Linux users can use the POSIX shell script:

```powershell
.\snip.ps1 up
```

```bat
url-shortener-docker.bat up
```

```sh
sh ./url-shortener-docker.sh up
```

Replace `up` with `down`, `restart`, `rebuild`, `logs`, `logs-follow`, `status`, or `clean`. Run the wrapper without a command (or with `help`) to list all options. The `clean` command removes the Compose containers and locally built project images, but preserves the `urlshortener-data` volume. To open the app, visit `http://localhost:3000`.
