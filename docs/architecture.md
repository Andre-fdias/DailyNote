# Daily Notes - Technical Architecture

## 1. Overview
Daily Notes is an Android application designed for firefighters, enabling them to manage occurrences, maps, schedules, fleets, and personnel directly from their devices. 
It follows the MVVM (Model-View-ViewModel) architectural pattern with a strong emphasis on offline-first capabilities.

## 2. Technology Stack
- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose
- **Dependency Injection**: Dagger Hilt
- **Local Database**: Room (SQLite) with WAL enabled for high concurrency and robust writes
- **Asynchronous Programming**: Kotlin Coroutines & Flow
- **Background Tasks**: WorkManager (for scheduled backups and syncing)
- **Maps**: MapLibre GL and Osmdroid for offline and custom map rendering
- **Cloud Backup**: Google Drive REST API (v3) with OAuth 2.0 via Google Sign-In

## 3. Architecture Layers
### 3.1 Data Layer (`data`)
Handles data retrieval, caching, and external APIs.
- **Local Data (`data.local`)**: Contains the Room database configuration (`AppDatabase`), DAOs, and Entities. The database stores all application state, from settings to daily occurrence records.
- **Repositories (`data.repository`)**: Implements interfaces from the domain layer. Provides a single source of truth for the ViewModel. Includes data mappers and local caching strategies.
- **Services (`data.service`)**: Handles integrations like `GoogleDriveBackupService` for zipping the SQLite database and syncing it to a private Google Drive AppData folder.

### 3.2 Domain Layer (`domain`)
Contains the core business logic of the app.
- **Models (`domain.model`)**: Pure Kotlin data classes representing the core business objects (e.g., `Ocorrencia`, `Militar`, `Viatura`).
- **Repositories Interfaces (`domain.repository`)**: Abstractions of data sources allowing the data layer to be mocked during testing.

### 3.3 UI Layer (`ui`)
Built entirely in Jetpack Compose.
- **Screens (`ui.screens`)**: Compose functions representing individual screens, organized by feature (e.g., `ocorrencias`, `mapaforca`, `agenda`, `settings`).
- **ViewModels**: State holders that handle user intents, communicate with repositories via Coroutines, and expose state via `StateFlow` to the UI.
- **Design System (`ui.designsystem`)**: Shared UI components, colors, and typography following Material 3 guidelines.

## 4. Key Systems
### 4.1 Backup & Persistence
- **Local Storage**: Room is configured with `JournalMode.WRITE_AHEAD_LOGGING` to prevent locking issues during heavy write operations.
- **Google Drive Integration**: A periodic `WorkManager` (every 24h) zips the database (`.db`, `-shm`, `-wal`) and local images, and uploads it to the user's hidden Google Drive AppData folder for secure, free backup.

### 4.2 Security
- **Biometric & PIN Authentication**: Controlled via DataStore preferences. On app startup (and resuming from the background), `MainActivity` observes a `LifecycleEventObserver` to lock the UI and enforce `BiometricPrompt` or PIN validation before exposing data.

### 4.3 Data Sharing
- **JSON Export/Import**: Using Android Intents, the app can serialize `Ocorrencia` structures into JSON strings, package them into `.json` files, and share them via WhatsApp or email. The app can intercept `.json` files, deserialize them, and offer the user options to Overwrite, Merge, or Duplicate records.
