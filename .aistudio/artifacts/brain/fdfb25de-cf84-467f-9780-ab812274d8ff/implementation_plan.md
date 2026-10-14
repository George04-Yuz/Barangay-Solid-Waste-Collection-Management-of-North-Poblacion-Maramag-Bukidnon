# North Poblacion CleanTrack: Android APK Release & Distribution Plan

Prepare the North Poblacion CleanTrack Android application for standalone release distribution via GitHub Releases, enabling residents and barangay officials to download and manually install `North-Poblacion-CleanTrack-v1.0.0.apk` without requiring the Google Play Store.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following release packaging and distribution decisions were confirmed:

- **Confirmed Decision 1 (Backend Connectivity & Offline Demo)**: Embedded live demo mode with a toggleable production API server configuration. This ensures defense presentations run flawlessly offline with pre-seeded data for all 18 puroks while allowing immediate connection to an HTTPS cloud backend.
- **Confirmed Decision 2 (GitHub Actions CI/CD Pipeline)**: An automated GitHub Actions workflow triggered on git tag push (`v*`) that builds the signed APK and automatically drafts/publishes a GitHub Release with the attached `North-Poblacion-CleanTrack-v1.0.0.apk` binary asset.
- **Confirmed Decision 3 (Resident Documentation)**: A comprehensive, non-technical `INSTALLATION_GUIDE.md` and repository `README.md` with step-by-step installation instructions, unknown sources approval walkthrough, and troubleshooting for installation or connectivity issues.

---

## 1. Overview & Core Concept

- **Product Identity**:
  - Application Display Name: **North Poblacion CleanTrack**
  - Package Identifier: `ph.northpoblacion.cleantrack`
  - Version Name: `1.0.0`
  - Version Code: `1`
  - Release Asset Name: `North-Poblacion-CleanTrack-v1.0.0.apk`
- **Target Distribution Channel**: GitHub Releases (Direct manual APK download for Android devices, minSdk 24 / Android 7.0+ through Android 16).
- **Core Value**: Direct accessibility for rural households in Barangay North Poblacion, Maramag, Bukidnon, avoiding Google Play registration barriers while maintaining cryptographic signing integrity and reproducible builds.

---

## 2. Technical Architecture & Release Workflow

```
┌────────────────────────────────────────────────────────────────────────┐
│                        GitHub Release Pipeline                         │
│                                                                        │
│   Developer Tag Push: git tag v1.0.0 && git push origin v1.0.0        │
│                                │                                       │
│                                ▼                                       │
│   GitHub Actions Workflow (.github/workflows/release-apk.yml)          │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ 1. Checkout repository & setup JDK 17                          │   │
│   │ 2. Run unit tests (:app:testDebugUnitTest)                     │   │
│   │ 3. Build signed Release APK (:app:assembleRelease)             │   │
│   │ 4. Package & rename artifact to:                               │   │
│   │    North-Poblacion-CleanTrack-v1.0.0.apk                       │   │
│   │ 5. Create GitHub Release v1.0.0 and upload APK binary asset    │   │
│   └────────────────────────────────────────────────────────────────┘   │
│                                │                                       │
│                                ▼                                       │
│   GitHub Releases Page: https://github.com/user/cleantrack/releases    │
│   - Release Notes: Features, 18 Puroks, Changelog, Verification Hash   │
│   - Asset: North-Poblacion-CleanTrack-v1.0.0.apk                       │
│                                │                                       │
│                                ▼                                       │
│   Resident / Official Android Device (Direct Download & Install)       │
│   - Browser downloads APK -> Allow 'Install from Unknown Sources'     │
│   - Launch North Poblacion CleanTrack v1.0.0                           │
│   - Toggle: [Embedded Demo Database] <---> [Production HTTPS Server]   │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Key Implementation Steps

### Step 1: Android Gradle Release Configuration
- Update `app/build.gradle.kts`:
  - `applicationId = "ph.northpoblacion.cleantrack"`
  - `versionCode = 1`
  - `versionName = "1.0.0"`
  - Configure release APK output renaming task or script to generate `North-Poblacion-CleanTrack-v1.0.0.apk`.
- Update `metadata.json` and `strings.xml` to reflect version `1.0.0`.

### Step 2: Production Server / Demo Mode Toggle
- Implement backend configuration in `CleanTrackViewModel`:
  - Toggle between **Embedded Local Data Engine (18 Puroks Pre-seeded)** and **Production HTTPS Server** (`https://api.northpoblacion.gov.ph` or custom URL).
  - Clear visual indicator in the UI displaying the current connection mode.
  - Safe error handling for network offline states.

### Step 3: About Screen Version Information
- Add version badge `v1.0.0 (Build 1)` and package identifier `ph.northpoblacion.cleantrack` in `AboutProjectDialog.kt` and settings.

### Step 4: GitHub Actions Release CI/CD Workflow
- Create `.github/workflows/release-apk.yml`:
  - Triggers on tag push `v*` and manual workflow dispatch.
  - Validates code, runs unit tests, builds the APK with Gradle, and attaches the binary to the release.

### Step 5: Comprehensive Documentation
- Create `README.md` with:
  - System architecture and capstone project description.
  - 18 Puroks directory and 3 user roles breakdown.
  - Local development and building instructions.
  - Release history and APK download links.
- Create `INSTALLATION_GUIDE.md`:
  - Step-by-step visual installation instructions for non-technical residents.
  - Sideloading permissions ("Install Unknown Apps") for Chrome, Files by Google, and Samsung Internet.
  - Google Play Protect reassurance (why warning appears and how to proceed safely without disabling protection).
  - Comprehensive troubleshooting guide.

### Step 6: Build Verification
- Execute Gradle build via `compile_applet` and run test suites to verify that `ph.northpoblacion.cleantrack` compiles with zero errors.
