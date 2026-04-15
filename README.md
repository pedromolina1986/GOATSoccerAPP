# GoatSoccerManager — Android App

Native Android application for managing soccer teams, leagues, matches, and standings, built with Kotlin and Jetpack Compose.

---

## Prerequisites

- [Android Studio](https://developer.android.com/studio) Hedgehog (2023.1.1) or newer
- Android SDK 34 (Android 14) — install via Android Studio SDK Manager
- JDK 17 (bundled with Android Studio)
- The [backend server](../backend/README.md) running locally on port 3000

---

## Installation

1. Open Android Studio.
2. Select **File > Open** and navigate to the `GoatSoccerManager` folder.
3. Android Studio will detect the Gradle project and prompt you to sync. Click **Sync Now**.
4. Wait for Gradle to download all dependencies automatically.

---

## Running the App

### On an Emulator

1. In Android Studio, open **Device Manager** (right toolbar or **View > Tool Windows > Device Manager**).
2. Create a virtual device with API level 26 or higher (Android 8.0+).
3. Start the emulator.
4. Click the **Run** button (green play icon) or press `Shift + F10`.

The app connects to the backend at `http://10.0.2.2:3000` — the Android emulator's alias for `localhost` on your machine.

### On a Physical Device

1. Enable **Developer Options** and **USB Debugging** on your Android device (Android 8.0+).
2. Connect the device via USB.
3. Select your device from the target dropdown in Android Studio.
4. Click **Run**.

> Make sure your phone and computer are on the same network and update the backend base URL in `RetrofitClient` to your machine's local IP address (e.g., `http://192.168.x.x:3000`).

### Via Command Line

```bash
cd GoatSoccerManager
./gradlew assembleDebug
# Install on a connected device or running emulator:
./gradlew installDebug
```

---

## Backend Dependency

The app requires the backend API to be running before you log in. See [backend/README.md](../backend/README.md) for setup instructions. Start it with:

```bash
cd ../backend
npm run dev
```

---

## Default Login Credentials

| Email | Password | Role |
|---|---|---|
| coach@goatsoccer.com | password123 | coach |
| player@goatsoccer.com | password123 | player |
| fan@goatsoccer.com | password123 | fan |

---

## Project Structure

```
GoatSoccerManager/
├── app/
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/goatsoccer/manager/
│           ├── data/
│           │   ├── api/         # Retrofit client, API service, auth interceptor
│           │   ├── local/       # SessionManager (JWT + user stored in SharedPreferences)
│           │   ├── model/       # Data classes: User, Team, Player, Match, League, Standing, Roast
│           │   └── repository/  # Repositories for each data domain
│           └── ui/
│               ├── auth/        # LoginActivity (app entry point)
│               ├── MainActivity # Hosts Compose navigation graph
│               └── screens/     # Home, Teams, Matches, Standings, Leagues, Roasts, Profile
├── build.gradle
└── settings.gradle
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM |
| Networking | Retrofit 2 + OkHttp + Gson |
| Async | Kotlin Coroutines |
| Navigation | Navigation Compose |
| State | ViewModel + LiveData |
| Auth storage | SharedPreferences (SessionManager) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |
