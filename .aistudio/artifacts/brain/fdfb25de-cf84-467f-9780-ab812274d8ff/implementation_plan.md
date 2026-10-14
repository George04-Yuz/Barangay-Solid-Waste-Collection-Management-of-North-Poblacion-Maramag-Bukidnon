# North Poblacion CleanTrack - Barangay Solid Waste Collection Management & Household Notification System

North Poblacion CleanTrack is a modern Android application engineered for Barangay North Poblacion, Maramag, Bukidnon, Philippines. It delivers a comprehensive solid waste collection operations platform serving all 18 designated puroks and sub-puroks, providing real-time schedule management, collection monitoring, resident complaints/missed-collection reporting, and an in-app notification center.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural decisions were confirmed during the interactive clarification phase:

- **Confirmed Decision 1 (Multi-Persona Demonstration)**: A top-level Persona Switcher bar enables the presenter to seamlessly switch between the three roles (**Barangay Administrator**, **Collection Worker / Truck Driver**, and **Resident / Household**) without logging out, while preserving distinct role permissions and views for defense evaluations.
- **Confirmed Decision 2 (Data Persistence & Cloud Sync)**: Built with **Firebase Firestore** real-time synchronization, with pre-seeded data for all 18 puroks and mock residents/schedules to ensure zero dead-ends and instantaneous offline-to-online continuity.
- **Confirmed Decision 3 (Notification System)**: An interactive **In-App Notification Center** with real-time banners, unread badge counters, and operational audit dispatch alerts (simulating schedule changes, collection start/completion, and report resolutions).

---

## 1. Overview & Core Concept

- **What It Does**: Digitizes and streamlines solid waste collection in Barangay North Poblacion, Maramag, Bukidnon. Administrators schedule and monitor truck dispatches across 18 puroks; drivers and collection workers update live operational statuses (Pending → In Progress → Completed); and residents monitor upcoming collections, receive timely alerts, and submit missed-collection reports with photo evidence.
- **Target Audience / Personas**:
  1. **Barangay Administrator / Official**: Manages collection schedules, assigns drivers and trucks, tracks real-time progress across all 18 puroks, resolves resident complaints, and analyzes operational completion metrics.
  2. **Collection Worker / Truck Driver**: Views daily assigned routes, transitions task statuses with timestamps (Pending, In Progress, Completed), reports vehicle breakdowns or route delays, and records completion notes.
  3. **Resident / Household**: Checks next scheduled collection for their purok, tracks truck collection progress in real time, receives barangay waste notices, submits missed garbage reports with attachments, and tracks resolution status.
- **Target Geography**: Exactly 18 designated puroks and sub-puroks of Barangay North Poblacion:
  *Purok 1, Purok 1A, Purok 1B, Purok 2, Purok 2A, Purok 3, Purok 4, Purok 5, Purok 5A, Purok 5B, Purok 6, Purok 6A, Purok 7, Purok 8, Purok 9, Purok 9A, Purok 9B, Purok 10*.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Administrator Workflow**:
   - **Dashboard**: High-level operational cards (Schedules Today, In Progress, Completed, Overdue, Unresolved Reports) + Purok status matrix.
   - **Schedule Management**: Create, edit, postpone, or publish collection schedules targeting specific puroks, assigning teams and trucks.
   - **Resident Complaint Management**: Filter by category (Uncollected Waste, Missed Schedule, Delay), review descriptions and attached photos, and update status (Under Review, In Progress, Resolved) with official barangay remarks.
   - **Reports & Analytics**: Daily/weekly completion rate charts, purok performance metrics, and CSV-style data export preview.
   - **Audit Logs**: Tamper-evident activity trail recording every schedule modification and status transition.

2. **Collection Worker / Driver Workflow**:
   - **Today's Route**: Clean list of assigned puroks with planned collection times and instructions.
   - **Action Controls**: One-tap "Start Collection" (transitions status to *In Progress* with server timestamp) and "Mark Completed" (requires confirmation, records end time and notes).
   - **Problem Reporting**: Quick dialog to log road blocks, vehicle issues, or weather delays.

3. **Resident / Household Workflow**:
   - **Home Hub**: Hero banner displaying the resident's purok, next scheduled pickup, and live status badge (*Pending*, *In Progress*, or *Completed*).
   - **File a Report**: Dedicated form to report missed collection or overflowing dump sites, with category selection, description, and photo upload.
   - **Schedule Calendar**: Calendar and timeline view of barangay collection days.
   - **Notification Center**: Inbox of broadcast announcements and purok-specific schedule alerts with unread pill indicators.

### Visual Identity & Theme

- **Aesthetic Direction**: Professional Philippine e-Government utility aesthetic — clean, trustworthy, eco-focused, and highly legible.
- **Color Palette**:
  - `Primary`: Deep Forest Green (`#1B5E20`) — environmental hygiene and governance.
  - `Secondary / Accent`: Emerald Green (`#2E7D32`) & Bright Mint (`#81C784`).
  - `Background`: Crisp Light Gray / Mint Mist (`#F7FAF7`) with Pure White surface cards (`#FFFFFF`).
  - `Status Tokens`:
    - *Pending*: Cool Slate (`#546E7A`)
    - *In Progress*: Vibrant Amber / Orange (`#EF6C00`)
    - *Completed*: Vibrant Teal / Green (`#2E7D32`)
    - *Urgent / Missed / Rejected*: Crimson Red (`#C62828`)
    - *Informational*: Royal Blue (`#1565C0`)
- **Typography & Accessibility**: Clean Material 3 typography with high contrast, touch targets meeting standard minimums (≥48dp), and responsive tablet/phone layouts.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Interactive Persona Switcher Toolbar**:
  - *Chosen Approach*: Persistent demonstration pill toolbar at the top of the screen allowing instant switching between Admin, Worker, and Resident profiles, along with specific purok selection for resident testing.
  - *Why*: Essential for academic defense and live stakeholder evaluation, proving cross-role interactions without needing three separate physical devices or repeated sign-out/sign-in flows.
- **Decision 2: Firebase Firestore with Seed Data & Offline Fallback**:
  - *Chosen Approach*: Firestore cloud database backed by an integrated offline cache and pre-populated catalog of all 18 North Poblacion puroks, fictional demonstration households, and realistic collection schedules.
  - *Why*: Meets the strict cloud sync requirement while ensuring the app works reliably and instantly even during network degradation.
- **Decision 3: In-App Simulated Notification Dispatcher**:
  - *Chosen Approach*: A rich in-app notification engine with snackbar banners, floating alert badges, and a searchable notifications inbox.
  - *Why*: Bypasses physical SMS gateway recurring fees and push token setup hurdles during defense while providing authentic visual and functional notification flows.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                   North Poblacion CleanTrack UI                        │
│                                                                        │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │   Persona Switcher: [Admin] [Worker: Juan D.] [Resident: P-1] │   │
│   └────────────────────────────────────────────────────────────────┘   │
│                                │                                       │
│          ┌─────────────────────┼─────────────────────┐                 │
│          ▼                     ▼                     ▼                 │
│   ┌───────────────┐     ┌───────────────┐     ┌───────────────┐        │
│   │ Admin Screen  │     │ Worker Screen │     │Resident Screen│        │
│   │ - Schedule Ops│     │ - Today Route │     │ - Next Pickup │        │
│   │ - Complaint Ctr│    │ - Status Timer│     │ - Live Status │        │
│   │ - Analytics   │     │ - Problem Log │     │ - File Report │        │
│   │ - Audit Trail │     │ - Task History│     │ - Inbox & News│        │
│   └───────────────┘     └───────────────┘     └───────────────┘        │
│          │                     │                     │                 │
│          └─────────────────────┼─────────────────────┘                 │
│                                ▼                                       │
│               CleanTrackViewModel (M3 / Compose)                       │
│                                │                                       │
│                                ▼                                       │
│               CleanTrackRepository (Data Layer)                        │
│                                │                                       │
│          ┌─────────────────────┴─────────────────────┐                 │
│          ▼                                           ▼                 │
│   Firestore Cloud Database                   In-App Notification       │
│   - /puroks (18 units)                       & Broadcast Manager       │
│   - /schedules                               - Status Alerts           │
│   - /resident_reports                        - Schedule Reminders      │
│   - /announcements                           - Report Resolution Toasts│
│   - /audit_logs                                                        │
└────────────────────────────────────────────────────────────────────────┘
```

### Core Entities & Collections

1. `Purok`: `id`, `name` (Purok 1 through Purok 10, including 1A, 1B, 2A, 5A, 5B, 6A, 9A, 9B), `householdCount`, `coordinatorContact`.
2. `CollectionSchedule`: `id`, `purokId`, `purokName`, `collectionDate`, `startTime`, `endTime`, `status` (`Pending`, `In Progress`, `Completed`), `state` (`Published`, `Postponed`, `Cancelled`), `driverName`, `truckPlate`, `instructions`, `startedAt`, `completedAt`, `notes`.
3. `ResidentReport`: `id`, `residentName`, `residentPhone`, `purokName`, `category` (`Uncollected Waste`, `Missed Schedule`, `Collection Delay`, `Illegal Dumping`, `Other`), `description`, `photoUri`, `status` (`Submitted`, `Under Review`, `In Progress`, `Resolved`, `Rejected`), `adminRemarks`, `timestamp`, `resolvedAt`.
4. `Announcement`: `id`, `title`, `content`, `purokTarget` ("All" or specific purok), `priority` (`Normal`, `Urgent`), `postedDate`.
5. `InAppNotification`: `id`, `targetRole`, `targetPurok`, `title`, `message`, `type` (`Schedule`, `StatusChange`, `ReportUpdate`, `Notice`), `isRead`, `timestamp`.
6. `AuditLog`: `id`, `actor`, `action`, `details`, `timestamp`.

---

## 5. Implementation Sequence

1. **Phase 1: Dependencies & Application Metadata**:
   - Update `metadata.json` with project name *"North Poblacion CleanTrack"* and description.
   - Synchronize `app/src/main/res/values/strings.xml` with matching application name.
   - Ensure Compose icons, Room/Firestore dependencies, and lifecycle artifacts in `build.gradle.kts` are active.
2. **Phase 2: Data Architecture & Domain Models**:
   - Construct robust Kotlin data models with full serialization and default parameters.
   - Implement repository layer with complete pre-seeded records for all 18 Puroks of North Poblacion.
3. **Phase 3: State Management & Reactive ViewModels**:
   - Build `CleanTrackViewModel` managing active persona, schedules, live updates, complaint ticketing, and notifications.
4. **Phase 4: User Interface & Component Polish**:
   - Top demonstration persona switcher bar.
   - Resident Dashboard, Schedule Calendar, Report Submission dialog with camera/gallery preview.
   - Worker Route List with Start/Complete action transitions and problem logging.
   - Admin Command Dashboard with scheduling modals, complaint resolution drawer, audit logs, and analytics export preview.
   - In-app notification center drawer.
5. **Phase 5: Verification & End-to-End Build**:
   - Compile and test the application with `compile_applet` to ensure zero compilation or lifecycle errors.
