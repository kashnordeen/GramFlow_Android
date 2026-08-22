# 🌿 GramFlow Mobile — Precision Inventory & FIFO Ledger (Android)

<div style="text-align: center;">
  <img src="app/src/main/res/drawable/app_logo.png" alt="GramFlow Logo" style="width: 120px; height: 120px; border-radius: 26px;" />
  <h3><b>GramFlow Android Native Application</b></h3>
  <p><i>Offline-First, High-Precision Gram/Batch Inventory & Real-Time Financial Ledger</i></p>
</div>

---

## 📱 Overview

**GramFlow Android** is a native Jetpack Compose mobile application built for rapid, reliable, offline-first inventory tracking, customer debt/loan bookkeeping, and automatic FIFO (First-In, First-Out) batch liquidation.

Designed with a high-contrast dark luxury interface, vibrant **Neon Lime** (`#C6FF00`) action triggers, and haptic feedback.

---

## 🔐 Credentials & Authentication

| Parameter | Value |
| :--- | :--- |
| **Registration Security Code** | `HEMP2026` (or `ADMIN2026`) |
| **Authorized Domain** | Must end with `@hemp.com` |
| **Default Admin Email** | `admin@hemp.com` |
| **Default Password** | `admin123` *(or create any account with the security code)* |
| **Session Security** | Automatic **30-Minute Inactivity Auto-Logout** with gesture activity tracking |

---

## 🚀 Key Features

- **⚡ Strict FIFO Inventory Engine**: Automatically tracks batches, remaining grams, cost per gram, and calculates true gross yield upon checkout.
- **⚡ Fast POS Terminal (Add Sale)**: 
  - Dynamic customer selector with quick legacy balance lookup.
  - Multi-batch FIFO deduction or specific batch allocation.
  - Instant balance computation (Gross, Discount, Received, Remaining Debt).
  - Bouncy animated success modal with haptic vibration feedback.
- **⚡ Debt & Loan Ledger (Customers)**:
  - Tracks legacy loans + live sales debt.
  - Instant partial or full debt settlement with customer-specific history.
- **⚡ Stock Vault Management**:
  - Ingest new flower / extract batches with cost basis, initial grams, and supplier reference.
  - Batch closing and restoration mechanics.
- **⚡ Master Transactions**:
  - Filter by All, Today, or specific customers with custom high-contrast filter pills.
  - One-tap transaction rollback (restores inventory to exact original batches).
- **⚡ Privacy Mode**: Global toggle to blur financial metrics and profit figures on the dashboard.
- **⚡ Native Haptics**: Subtle vibrations on tab switches, profile interactions, and transaction processing.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.0+
- **UI Toolkit:** Jetpack Compose & Material 3
- **Architecture:** Clean Architecture (MVVM + Repository Pattern)
- **Local Database:** Room Database with SQLite (Local-First, 100% Offline Capable)
- **Asynchronous Flow:** Kotlin Coroutines & Reactive `StateFlow` / `SharedFlow`
- **Navigation:** Jetpack Navigation Compose
- **Design System:** Custom Dark Luxury Theme (`#111312` Canvas, `#191C1B` Surface, `#C6FF00` Neon Lime)

---

## 📦 How to Build the APK

### Method 1: Using Android Studio (Recommended)
1. Open **Android Studio**.
2. Select **Open** and choose the `d:/PMS/gramflow-android` folder.
3. Wait for Gradle Sync to complete.
4. In the top menu, go to:
   - **`Build`** ➔ **`Build Bundle(s) / APK(s)`** ➔ **`Build APK(s)`**.
5. Once built, click the **"locate"** popup link in the bottom-right corner.
6. The generated APK will be at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

### Method 2: Using Command Line (Terminal / PowerShell)

Open PowerShell inside `d:/PMS/gramflow-android`:

```powershell
# Build Debug APK
.\gradlew.bat assembleDebug

# Output APK Location:
# app\build\outputs\apk\debug\app-debug.apk
```

For release APK:
```powershell
.\gradlew.bat assembleRelease
```

---

## 📂 Project Structure

```
gramflow-android/
├── app/
│   ├── src/main/
│   │   ├── java/com/gramflow/app/
│   │   │   ├── data/
│   │   │   │   ├── local/          # Room DB, DAOs, Entities (Batch, Sale, Customer, User)
│   │   │   │   └── repository/     # Inventory, Auth, Customer, Settings Repositories
│   │   │   ├── ui/
│   │   │   │   ├── components/     # TopBar, BottomNav, Modals, Dialogs
│   │   │   │   ├── navigation/     # NavHost & Screen definitions
│   │   │   │   ├── screens/        # Splash, Login, Signup, Dashboard, AddSale, Stock, etc.
│   │   │   │   └── theme/          # Luxury Dark Color Palette & Typography
│   │   │   ├── util/               # VibrationHelper, Formatters
│   │   │   ├── viewmodel/          # StateFlow ViewModels
│   │   │   ├── GramFlowApp.kt      # Application Class & Dependency Graph
│   │   │   └── MainActivity.kt     # Single Activity Host
│   │   ├── res/
│   │   │   ├── drawable/           # App logo, launcher vectors, custom assets
│   │   │   ├── mipmap-*/           # Multi-resolution launcher icon PNGs
│   │   │   └── values/             # Colors, themes, string resources
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
└── README.md
```

---

## 🔒 Security & Privacy

- **Local Storage:** SQLite Room Database stored securely in the app's internal sandbox.
- **Passwords:** SHA-256 / BCrypt encrypted before local storage.
- **Inactivity Timeout:** 30-minute idle watchdog automatically logs out session and prompts for re-authentication.

---

<div style="text-align: center;">
  <b>GramFlow</b> · <i>Engineered for Maximum Precision</i>
</div>
