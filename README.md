# North Poblacion CleanTrack

**Cross-Platform Barangay Solid Waste Collection Management and Household Notification System for North Poblacion, Maramag, Bukidnon**

A modern Android application engineered to modernize garbage collection scheduling, real-time collection monitoring, household alerts, missed-collection reporting, and operational analytics for **Barangay North Poblacion, Municipality of Maramag, Province of Bukidnon, Philippines**.

[![Android Build & Release APK](https://img.shields.io/badge/Release%20APK-v1.0.0-2E7D32?style=for-the-badge&logo=android&logoColor=white)](https://github.com/north-poblacion-cleantrack/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B%20(API%2024%2B)-3DDC84?style=for-the-badge&logo=android)](https://developer.android.com/)

---

## 📍 Geographic Scope & 18 Designated Puroks

The system covers all 18 official puroks and sub-puroks of Barangay North Poblacion:

1. **Purok 1** (142 Households)
2. **Purok 1A** (98 Households)
3. **Purok 1B** (115 Households)
4. **Purok 2** (180 Households)
5. **Purok 2A** (124 Households)
6. **Purok 3** (165 Households)
7. **Purok 4** (210 Households)
8. **Purok 5** (195 Households)
9. **Purok 5A** (130 Households)
10. **Purok 5B** (110 Households)
11. **Purok 6** (175 Households)
12. **Purok 6A** (140 Households)
13. **Purok 7** (220 Households)
14. **Purok 8** (190 Households)
15. **Purok 9** (160 Households)
16. **Purok 9A** (105 Households)
17. **Purok 9B** (95 Households)
18. **Purok 10** (185 Households)

---

## 👥 Three User Personas & Core Workflows

### 1. Barangay Administrator / Official
- **Operations Command Dashboard:** High-level overview cards tracking covered puroks, registered households, collections today, pending/in-progress/completed trucks, and unresolved tickets.
- **Schedule Management:** Create, publish, postpone, or cancel collection schedules with assigned drivers, truck plates, planned time windows, and route instructions.
- **Complaint & Report Resolution:** Review resident missed-collection reports, examine attached photo evidence, update status (*Under Review*, *In Progress*, *Resolved*, *Rejected*), and issue official remarks.
- **Barangay Broadcasts:** Post public announcements targeting all puroks or specific zones.
- **Analytics & CSV Export:** View real-time completion rates and generate full CSV reports for official municipal submission.
- **System Audit Trail:** Immutable chronological log of all schedule changes, status transitions, and administrative actions.

### 2. Collection Worker / Truck Driver
- **Assigned Route:** Daily list of assigned puroks with time windows and special instructions.
- **"Start Collection" Button:** One-tap action that transitions status from *Pending* to *In Progress* and stamps the server start time.
- **"Mark Completed" Button:** Requires confirmation and records the final completion timestamp, collected tonnage, and route notes.
- **"Report Issue" Button:** Logs operational delays (mechanical breakdowns, road blockages, heavy flooding).

### 3. Resident / Household
- **Pickup Dashboard:** Real-time collection countdown, assigned truck info, and live status badge (*Pending*, *In Progress*, *Completed*).
- **Interactive 18-Purok Switcher:** Residents can immediately check schedules and history for their specific purok.
- **Report Uncollected Waste:** Form to file missed-collection complaints with category selection, address details, and optional photo attachment.
- **Ticket Tracking:** Follow the progress of submitted complaints and read official Barangay responses.
- **In-App Notification Center:** Push alert inbox for schedule updates, collection start notices, and resolution toasts.

---

## 📦 Download & Installation for Android Devices

For non-technical residents and evaluators, read the **[Resident Installation Guide](INSTALLATION_GUIDE.md)**.

### Quick Install Steps:
1. Go to the [Releases](https://github.com/north-poblacion-cleantrack/releases) page.
2. Download **`North-Poblacion-CleanTrack-v1.0.0.apk`**.
3. Open the downloaded file on your Android phone.
4. Allow installation from unknown sources when prompted by your browser or file manager.
5. Tap **Install** and open **North Poblacion CleanTrack**.

---

## 🏗️ Technology Stack & Architecture

- **Language:** Kotlin 2.2
- **UI Framework:** Jetpack Compose with Material Design 3 (M3)
- **Architecture:** Model-View-ViewModel (MVVM) + Repository Pattern
- **State Management:** Kotlin Coroutines & `MutableStateFlow` / `StateFlow`
- **Application Package ID:** `ph.northpoblacion.cleantrack`
- **Minimum Android SDK:** 24 (Android 7.0 Nougat)
- **Target Android SDK:** 36 (Android 16)
- **Image Loading:** Coil Compose
- **Design Tokens:** Eco-Friendly Green (`#1B5E20`), Emerald (`#2E7D32`), Clean White (`#FFFFFF`)

---

## 💻 Developer Setup & Build Commands

### 1. Prerequisites
- Java Development Kit (JDK) 17+
- Android SDK with platform tools (API 36)
- Gradle 8.11+ (or system Gradle)

### 2. Clone Repository
```bash
git clone https://github.com/your-username/north-poblacion-cleantrack.git
cd north-poblacion-cleantrack
```

### 3. Run Automated Unit Tests
```bash
gradle :app:testDebugUnitTest
```

### 4. Build Android Debug / Demo APK
```bash
gradle :app:assembleDebug
```
The generated APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### 5. Build and Package Release APK
```bash
gradle :app:copyReleaseApk
```
This packages the standalone release APK to:
`release-apk/North-Poblacion-CleanTrack-v1.0.0.apk`

---

## 🚀 GitHub Actions CI/CD Release Pipeline

Automated packaging is configured in `.github/workflows/release-apk.yml`.

### To Publish a New GitHub Release:
1. Commit all verified changes to the `main` branch.
2. Create and push a version tag:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
3. GitHub Actions will automatically:
   - Run unit test suites
   - Compile the signed Android APK
   - Rename artifact to `North-Poblacion-CleanTrack-v1.0.0.apk`
   - Compute SHA-256 checksums
   - Publish the GitHub Release with downloadable binary assets

---

## ⚖️ Legal & Regulatory Compliance

- **Republic Act No. 9003 (Ecological Solid Waste Management Act of 2000):** Promotes household waste segregation (Biodegradable, Residual, Recyclable) across Barangay North Poblacion.
- **Republic Act No. 10173 (Philippine Data Privacy Act of 2012):** Protects resident personal information, phone numbers, and home addresses submitted through complaints. No public resident directory is exposed.

---

## 🎓 Capstone Project Information

- **Degree Program:** Bachelor of Science in Information Technology (BS IT)
- **Capstone Title:** *Cross-Platform Barangay Solid Waste Collection Management and Household Notification System for North Poblacion, Maramag, Bukidnon*
- **Target Beneficiary:** Barangay North Poblacion, Municipality of Maramag, Province of Bukidnon
