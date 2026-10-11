# Marketing Tracker - Android Application

A modern, offline-first Android application designed to track marketing and grocery shopping lists with intelligent auto-suggestions, dynamic categories, customizable units, date shifting, and scheduled notification reminders.

---

## Key Features

1. **Intelligent Suggestions**:
   - As you type item names, auto-complete suggestions appear based on previously entered items.
   - Pre-seeded with common marketing essentials (Milk, Rice, Tomatoes, Eggs, Cooking Oil, etc.).

2. **Dynamic Categories**:
   - Organized into categories (Vegetables, Fruits, Dairy & Eggs, Groceries & Staples, Meat & Seafood, Bakery, Beverages, Snacks, Spices, Household, Personal Care, Others).
   - Easily add new custom categories with custom colors.
   - Filter marketing items by category using horizontal filter chips.

3. **Quantities & Customizable Units**:
   - Enter decimal or integer quantities (e.g., `2`, `1.5`, `500`).
   - Default units: `kg`, `g`, `L`, `ml`, `pc`, `pkt`, `doz`, `box`, `btl`, `bunch`, `can`.
   - Add your own custom units on the fly (e.g., `bundle`, `carton`, `pack`).

4. **Mark as Bought**:
   - Single-tap checkbox to mark items as bought with strikethrough styling.
   - Header instantly displays live count of unbought vs bought items.

5. **Flexible Date Shifting**:
   - Shift items seamlessly between dates:
     - Shift to Today
     - Shift to Tomorrow
     - Shift +2 Days
     - Shift to Next Sunday
     - Custom Date Picker
   - **Batch Shift**: Shift all unbought items from any day to Tomorrow/Today with one tap.

6. **Automated Notification Engine**:
   - **Schedule Rules**:
     - **Sundays & Holidays**: Notifications trigger at **7:00 AM**.
     - **Other Days (Mon–Sat)**: Notifications trigger at **5:00 PM** (17:00).
   - **Clean Reminders**: Already bought items are automatically excluded.
   - **No Empty Notifications**: If there are no items to buy for a day, no notification is triggered.
   - **Rich Content**: Today's items and their exact quantities are clearly listed (`• Item - Qty Unit`).
   - **Toast Alert**: When notifications fire, an instant Toast message is displayed on screen.
   - **Auto-Cleanup**: Notifications and database logs older than 1 week (7 days) are automatically removed and cancelled.
   - **Holiday Manager**: Built-in support for standard public holidays, plus the ability to add regional/custom holidays.
   - **Instant Test Trigger**: Menu option to test the notification and toast alert immediately.

7. **CI/CD with GitHub Actions**:
   - Included `.github/workflows/build.yml` automatically compiles the debug APK on every commit/push and attaches the APK as an artifact ready for download.

---

## Architecture & Tech Stack

- **Language**: Kotlin 1.9.22
- **UI Architecture**: MVVM (Model-View-ViewModel) with Android Architecture Components
- **Local Persistence**: Room SQLite Database 2.6.1 with Coroutines & StateFlow
- **Design System**: Material Design 3 (`Theme.Material3.DayNight.NoActionBar`)
- **Scheduling**: Android `AlarmManager` (`setExactAndAllowWhileIdle`) + `BroadcastReceiver` + `BootReceiver` (survives phone reboot)
- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 34)
- **Build System**: Gradle 8.2 (Kotlin DSL) + Android Gradle Plugin 8.2.2

---

## Project Structure

```
MarketingTrackerApp/
├── .github/
│   └── workflows/
│       └── build.yml               # GitHub Actions CI workflow to auto-build APK
├── app/
│   ├── build.gradle.kts            # App dependencies (Room, Lifecycle, Material 3)
│   ├── src/main/
│   │   ├── AndroidManifest.xml     # Permissions (POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM)
│   │   ├── java/com/example/marketingtracker/
│   │   │   ├── MarketingApplication.kt
│   │   │   ├── model/              # Room Entities (MarketingItem, Category, Unit, Holiday, Log)
│   │   │   ├── data/               # MarketingDao, AppDatabase, MarketingRepository
│   │   │   ├── util/               # DateUtils (ISO dates, Sunday check, formatting)
│   │   │   ├── notification/       # NotificationScheduler, NotificationReceiver, HolidayManager, BootReceiver
│   │   │   └── ui/                 # MainActivity, MainViewModel, Adapters, Dialogs
│   │   └── res/
│   │       ├── layout/             # Clean Material 3 XML layouts
│   │       ├── drawable/           # Vector drawables and backgrounds
│   │       └── values/             # Colors, Strings, Themes
├── build.gradle.kts                # Root build file
├── settings.gradle.kts             # Module inclusion & repository definitions
├── gradle.properties
├── gradlew                         # POSIX wrapper script
└── gradlew.bat                     # Windows wrapper script
```

---

## How to Build and Run

### 1. Build via GitHub Actions (Recommended)
1. Push this repository to GitHub.
2. Navigate to the **Actions** tab in your GitHub repository.
3. The `Build Android APK` workflow will run automatically on push.
4. Once completed, click on the workflow run and download the **`marketing-tracker-debug-apk`** artifact.

### 2. Build via Android Studio
1. Open Android Studio.
2. Select **File > Open** and choose the `MarketingTrackerApp` folder.
3. Let Gradle sync dependencies.
4. Connect an Android device or emulator (Android 8.0+).
5. Click the green **Run (Shift + F10)** button.

### 3. Build via Command Line
```bash
# On Linux / macOS
chmod +x gradlew
./gradlew assembleDebug

# On Windows
gradlew.bat assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## Testing Notification & Toast Behavior

1. Open the app and add 1 or 2 items for today's date.
2. Click the top-right overflow menu (**⋮**) and tap **"Test Notification & Toast Now"**.
3. A Toast alert (`Marketing Alert: X items to buy today!`) will immediately appear, and a rich Android notification will be posted in the status bar listing the items and their quantities.
4. Mark the items as bought and trigger the test again: the app will inform you that there are no unbought items, and no notification will be posted.
