# Regain

A native Android lockdown timer app built with Kotlin and Jetpack Compose.

## Features

- **Lockdown Timer** — Set hours/minutes, tap Start. A foreground service keeps the countdown alive even when the app is backgrounded.
- **State Persistence** — Lock end-timestamp saved with DataStore; survives app restarts and device reboots.
- **In-App Dialer** — Keypad, Contacts (READ_CONTACTS), and Call Log (READ_CALL_LOG) tabs with search. Calls placed via standard Android call intent.
- **Session History** — Every lockdown session stored in Room DB with start time, duration, and completion status.
- **Settings** — Toggle persistent notification, lock persistence, and quick links to Battery Optimization / App Settings.

## Requirements

- Android Studio Ladybug (2024.2+) or newer
- JDK 17
- Android SDK 35
- Minimum device: Android 8.0 (API 26)

## Build Locally

```bash
# Clone the repo
git clone https://github.com/<your-username>/regain-android.git
cd regain-android

# Create a signing key (dev only)
mkdir -p app/signing
keytool -genkey -v \
  -keystore app/signing/signing-key.jks \
  -alias regain -keyalg RSA -keysize 2048 -validity 10000

# Create local.properties
echo "signing.storeFile=signing/signing-key.jks" >> local.properties
echo "signing.storePassword=YOUR_PASS" >> local.properties
echo "signing.keyAlias=regain" >> local.properties
echo "signing.keyPassword=YOUR_PASS" >> local.properties

# Build debug APK
./gradlew :app:assembleDebug

# Build release APK
./gradlew :app:assembleRelease
```

The APK will be at `app/build/outputs/apk/release/`.

## GitHub Actions

Pushing to `main` or `dev` automatically:
1. Builds a signed release APK
2. Uploads it as a workflow artifact
3. Creates a GitHub Release with the APK attached

Add `SIGNING_STORE_PASSWORD` and `SIGNING_KEY_PASSWORD` as repository secrets for production signing. If absent, the CI generates a one-off debug keystore.

## Architecture

```
app/src/main/java/com/ryzix/regain/
├── data/
│   ├── AppContainer.kt          # Manual DI container
│   ├── datastore/LockDataStore.kt
│   └── db/                      # Room: entity, DAO, database
├── model/                       # LockSession, LockState
├── repository/                  # LockRepository, HistoryRepository
├── service/
│   ├── LockForegroundService.kt # Persistent notification + countdown
│   └── BootReceiver.kt          # Resume lock after reboot
├── navigation/NavGraph.kt
├── ui/
│   ├── theme/                   # Color, Type, Theme (Material 3 dark)
│   ├── components/BottomNavBar.kt
│   └── screens/                 # Home, Dialer, History, Settings
└── viewmodel/                   # HomeViewModel, DialerViewModel, etc.
```

## Known Limitations

- **OEM background killing**: On Xiaomi, Huawei, and some Samsung devices the OS may kill background services regardless. Users are guided via Settings → Battery Optimization to exempt the app.
- **Screen pinning**: The app requests `startLockTask()` on lockdown; the system shows its own "This app is pinned" dialog. Users can still unpin via the standard gesture — Android does not allow fully preventing this without Device Policy Manager (MDM/kiosk mode).
- **Call log on Android 11+**: READ_CALL_LOG is a restricted permission. The app requests it at runtime and gracefully shows an empty list if denied.

## Developer

Rohan Dora
