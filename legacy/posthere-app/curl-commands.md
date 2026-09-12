# Curl Commands for Testing Endpoints

This file contains curl commands to test all endpoints in the Posthere application.

> [!NOTE]
> Default Spring Boot port is **8080**. If you've configured a different port, replace `8080` with your configured port in all commands below.

---

## 1. HelloController Endpoints

### GET /hello
Returns a simple greeting message.

```bash
curl -X GET http://localhost:8080/hello
```

**Expected Response:**
```
Hello Gemini!!
```

---

## 2. DetailsController Endpoints

### GET /api/details
Get all details from the database.

```bash
curl -X GET http://localhost:8080/api/details
```

**Expected Response:**
```json
[
  {
    "fname": "John",
    "lname": "Doe",
    "city": "New York"
  }
]
```

---

### GET /api/details/{id}
Get a specific detail by ID.

```bash
curl -X GET http://localhost:8080/api/details/1
```

**Expected Response:**
```json
{
  "fname": "John",
  "lname": "Doe",
  "city": "New York"
}
```

---

### POST /api/details
Create a new detail entry.

**Windows Command Prompt:**
```bash
curl -X POST http://localhost:8080/api/details -H "Content-Type: application/json" -d "{\"fname\":\"Alice\",\"lname\":\"Smith\",\"city\":\"London\"}"
```

**PowerShell:**
```powershell
curl -X POST http://localhost:8080/api/details -H "Content-Type: application/json" -d '{\"fname\":\"Alice\",\"lname\":\"Smith\",\"city\":\"London\"}'
```

**Git Bash / WSL:**
```bash
curl -X POST http://localhost:8080/api/details \
  -H "Content-Type: application/json" \
  -d '{
    "fname": "Alice",
    "lname": "Smith",
    "city": "London"
  }'
```

**Expected Response (HTTP 201):**
```json
{
  "fname": "Alice",
  "lname": "Smith",
  "city": "London"
}
```

---

## Quick Test Script

### PowerShell Script
Save as `test-endpoints.ps1`:

```powershell
Write-Host "Testing Hello Endpoint..." -ForegroundColor Green
curl http://localhost:8080/hello
Write-Host "`n"

Write-Host "Testing GET All Details..." -ForegroundColor Green
curl http://localhost:8080/api/details
Write-Host "`n"

Write-Host "Testing POST Create Detail..." -ForegroundColor Green
curl -X POST http://localhost:8080/api/details -H "Content-Type: application/json" -d '{\"fname\":\"Test\",\"lname\":\"User\",\"city\":\"TestCity\"}'
Write-Host "`n"

Write-Host "Testing GET Detail by ID..." -ForegroundColor Green
curl http://localhost:8080/api/details/1
```

### Bash Script
Save as `test-endpoints.sh`:

```bash
#!/bin/bash

echo "Testing Hello Endpoint..."
curl http://localhost:8080/hello
echo -e "\n"

echo "Testing GET All Details..."
curl http://localhost:8080/api/details
echo -e "\n"

echo "Testing POST Create Detail..."
curl -X POST http://localhost:8080/api/details \
  -H "Content-Type: application/json" \
  -d '{
    "fname": "Test",
    "lname": "User",
    "city": "TestCity"
  }'
echo -e "\n"

echo "Testing GET Detail by ID..."
curl http://localhost:8080/api/details/1
```

---

## Tips

1. **Check Application Status:**
   ```bash
   curl http://localhost:8080/actuator/health
   ```
   *(Only if Spring Actuator is enabled)*

2. **Pretty Print JSON Response:**
   ```bash
   curl http://localhost:8080/api/details | python -m json.tool
   ```

3. **Save Response to File:**
   ```bash
   curl http://localhost:8080/api/details -o response.json
   ```

4. **View Response Headers:**
   ```bash
   curl -i http://localhost:8080/api/details
   ```

5. **Verbose Mode (for debugging):**
   ```bash
   curl -v http://localhost:8080/api/details
   ```
