# Native Android Kotlin Property Management System

A robust, enterprise-ready native Android application built with **Kotlin**, **Jetpack Compose**, **MVVM Architecture**, and **Room Database** for tracking, managing, and persisting real estate property portfolios.

---

## 📋 Table of Contents
- [Project Overview](#-project-overview)
- [Architecture & Data Flow](#-architecture--data-flow)
- [Package Structure](#-package-structure)
- [Features](#-features)
- [Setup & Build Instructions](#-setup--build-instructions)
- [Database Inspection](#-database-inspection)
- [Team Contributions](#-team-contributions)

---

## 🏢 Project Overview

The **Property Management System** provides real estate managers and property owners with a streamlined tool to register, track, and monitor residential and commercial properties. The application calculates portfolio statistics in real-time—including total property counts, occupied vs. vacant metrics, and total expected monthly rental income—while persisting all records across application restarts using an asynchronous SQLite database engine.

---

## 🏗 Architecture & Data Flow

This application adheres strictly to modern Android architectural recommendations using **MVVM (Model-View-ViewModel)** coupled with the **Repository Pattern** and **Room Data Persistence**.

```
┌────────────────────────────────────────────────────────┐
│                   UI Layer (Compose)                   │
│         MainActivity / PropertyDashboardScreen          │
└───────────────────────────┬────────────────────────────┘
                            │ (Observes LiveData)
                            ▼
┌────────────────────────────────────────────────────────┐
│             ViewModel (PropertyViewModel)              │
│  - Executes Input Validation                           │
│  - Exposes Transformed LiveData (map)                  │
│  - Launches Coroutines on Dispatchers.IO               │
└───────────────────────────┬────────────────────────────┘
                            │ (Calls Repository)
                            ▼
┌────────────────────────────────────────────────────────┐
│            Repository (PropertyRepository)             │
│  - Decouples UI Logic from Local Storage               │
│  - Exposes Room LiveData Stream as Single Truth        │
└───────────────────────────┬────────────────────────────┘
                            │ (Executes DAO Operations)
                            ▼
┌────────────────────────────────────────────────────────┐
│             Local Data Source (Room Database)          │
│  - PropertyDao (Insert, Update, Delete, Query)         │
│  - PropertyDatabase (Room Database Singleton)          │
│  - Property Entity (SQLite Table: "properties")        │
└────────────────────────────────────────────────────────┘
```

### Architectural Highlights
- **Single Source of Truth**: `PropertyDao.getAllProperties()` emits a continuous stream of `LiveData<List<Property>>`. Transformations automatically calculate statistics whenever Room data changes.
- **Asynchronous Coroutine Persistence**: Writes (`insertProperty`, `updateProperty`, `deleteProperty`) run asynchronously inside `viewModelScope.launch(Dispatchers.IO)` to maintain a silky smooth 60fps UI.
- **Strict Data Validation**: Prevents invalid inputs (missing fields, blank strings, non-numeric or negative rent) before persisting to the database.

---

## 📁 Package Structure

```
com.example.propertymanagementsystem
│
├── data
│   ├── local
│   │   ├── PropertyDao.kt         # Room Data Access Object (Insert, Update, Delete, LiveData Query)
│   │   └── PropertyDatabase.kt    # RoomDatabase Singleton instance initializer
│   ├── model
│   │   └── Property.kt            # Room @Entity representing the 'properties' database table
│   └── repository
│       └── PropertyRepository.kt  # Repository layer delegating Room DAO methods
│
├── ui
│   ├── theme
│   │   ├── Color.kt               # Application color definitions
│   │   ├── Theme.kt               # Material 3 Theme setup
│   │   └── Type.kt                # Typography configuration
│   └── viewmodel
│       └── PropertyViewModel.kt   # AndroidViewModel managing LiveData transformations & coroutines
│
└── MainActivity.kt                # Jetpack Compose UI Dashboard & entry point
```

---

## ✨ Features.

- **Property Registration**: Add new residential and commercial properties with name, location, type, monthly rent, caretaker contact, and occupancy status.
- **Real-Time Portfolio Statistics**:
  - Total property count (`propertyCountText`)
  - Occupancy breakdown (`occupancySummaryText`)
  - Financial overview showing formatted monthly revenue (`totalMonthlyRentText`)
- **Persistent Storage**: Full database persistence using Room SQLite; data remains available across app kills and restarts.
- **Reactive UI**: Data changes automatically update Jetpack Compose components via `observeAsState()`.

---

## 🚀 Setup & Build Instructions

### Prerequisites
- **Android Studio**: Ladybug / Jellyfish (2024.1+) or newer
- **JDK**: Version 17 or higher
- **Android SDK**: API 34+ (Minimum SDK: 24)

### Building & Running the Project
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/example/Property-Management-Project.git
   cd Property-Management-Project
   ```
2. **Open in Android Studio**: Launch Android Studio and select **Open** -> choose the project root folder.
3. **Sync Gradle**: Allow Gradle to download dependencies and build index.
4. **Run Unit Tests**:
   ```bash
   ./gradlew test
   ```
5. **Run the Application**:
   Select an emulator or connected physical Android device and click **Run 'app'** (`Shift + F10`).

---

## 🔍 Database Inspection

To view, inspect, and query the underlying SQLite database live while the app is running:

1. Open Android Studio.
2. Navigate to **View** > **Tool Windows** > **App Inspection**.
3. Select the **Database Inspector** tab.
4. Select your running process `com.example.propertymanagementsystem`.
5. Expand `property_management_db` > `properties` to view live database rows.

---

## 👥 Team Contributions

| Team Member | Role | Key Contributions |
| :--- | :--- | :--- |
| **Owen Kalanja** | Project Lead, Auth, Navigation & Release | Led project architecture setup, authentication flow design, navigation routing, and release engineering. |
| **Adam Jerry** | Room Database Layer | Architected the Room SQLite persistence layer including `Property` entity schema, `PropertyDao`, and `PropertyDatabase` singleton. |
| **David Rodgers** | ViewModel & LiveData Logic | Designed `PropertyViewModel`, business validation rules, and reactive LiveData transformations (`propertyCountText`, `occupancySummaryText`, `totalMonthlyRentText`, `allPropertiesText`). |
| **Grace Ngechu** | UI Fragments & Data Binding | Built responsive Jetpack Compose Material 3 UI screens, form components, dynamic data binding, and feedback notification UI. |
| **Lovine Muema** | Repository, Coroutine Persistence & Documentation | Built `PropertyRepository`, integrated Kotlin Coroutines (`viewModelScope`, `Dispatchers.IO`) for background database persistence, and compiled technical documentation (`README.md`). |

---

## 🛠 Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Repository Pattern
- **Database**: Room Persistence Library (SQLite)
- **Async & Concurrency**: Kotlin Coroutines (`Dispatchers.IO`, `viewModelScope`)
- **State Management**: LiveData & `observeAsState()`
