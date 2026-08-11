# MessMate
A Complete Android Mess Management & Meal Accounting App.

## Backend Setup (Spring Boot)
### 1. MongoDB Atlas Setup
- Create a Cluster in MongoDB Atlas.
- Add a new Database user and password.
- Whitelist your IP address (0.0.0.0/0 for all).
- Copy the connection string.

### 2. Backend Environment Variables
Set the following environment variables in your system or IDE:
- `MONGODB_URI`: Your MongoDB Atlas connection string (e.g. `mongodb+srv://user:pass@cluster.mongodb.net/messmate`).
- `JWT_SECRET`: A secure random 256-bit key for JWT generation.

### 3. Running the Backend
From the `backend/` directory:
```bash
# Windows
.\mvnw.cmd spring-boot:run
# Mac/Linux
./mvnw spring-boot:run
```

## Android App Setup
### 1. Requirements
- Android Studio Iguana or later.
- Java 17.

### 2. Configuration
- Open the `android/` directory in Android Studio.
- Sync Gradle to download dependencies (Jetpack Compose, Room, Retrofit).
- Configure the `BASE_URL` in Retrofit (inside the networking package) to point to your running backend (e.g. `http://10.0.2.2:8080/api/` for emulator).

### 3. Running the App
- Press **Run 'app'** in Android Studio to deploy to your emulator or physical device.

## API Documentation
| Entity | Endpoints |
| --- | --- |
| **Auth** | `POST /api/auth/login`, `POST /api/auth/register` |
| **Messes** | `POST /api/messes`, `POST /api/messes/join` |
| **Meals** | `POST /api/messes/{messId}/meals`, `GET /api/messes/{messId}/meals/history` |
| **Expenses** | `POST /api/messes/{messId}/expenses` |
| **Payments** | `POST /api/messes/{messId}/payments` |
| **Balance** | `GET /api/messes/{messId}/balance/me` |
| **Settlement** | `POST /api/messes/{messId}/settlements/generate`, `POST /api/messes/{messId}/settlements/{id}/close` |
