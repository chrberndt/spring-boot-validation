# README

## How To Use

Clone the repo and run the application with `./gradlew bootRun`

Database is available at http://localhost:8080/h2-console, use the following parameters to login:

```bash
Driver Class: org.h2.Driver # default
JDBC URL: jdbc:h2:mem:test # default
User Name: sa # default
Password: # empty (default)
```

## Validation Behaviour

Sending a POST request

### Without Request Body

```bash
curl \
  --header "Content-Type: application/json" \
  --request POST \
  --silent \
  http://localhost:8080/users \
| jq
```

will result in the following error response:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Failed to read request",
  "instance": "/users"
}
```

