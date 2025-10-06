# StudyFlow Android - Project Structure

## 📁 Complete File Structure

```
StudyFlow-Android/
├── 📄 build.gradle                    # Root build configuration
├── 📄 settings.gradle                 # Project settings
├── 📄 gradle.properties              # Gradle properties
├── 📄 gradlew.bat                    # Gradle wrapper (Windows)
├── 📄 local.properties              # Local SDK path (auto-generated)
├── 📄 README.md                      # Project documentation
├── 📄 PROJECT_STRUCTURE.md           # This file
│
├── 📁 gradle/wrapper/
│   └── 📄 gradle-wrapper.properties  # Gradle wrapper config
│
└── 📁 app/
    ├── 📄 build.gradle               # App module build config
    ├── 📄 proguard-rules.pro        # ProGuard rules
    │
    └── 📁 src/main/
        ├── 📄 AndroidManifest.xml    # App manifest
        │
        ├── 📁 java/com/studyflow/tracker/
        │   ├── 📄 MainActivity.kt              # Main activity
        │   ├── 📄 StudyFlowApplication.kt      # Application class
        │   ├── 📄 MainViewModelFactory.kt     # ViewModel factory
        │   │
        │   ├── 📁 data/
        │   │   ├── 📁 database/
        │   │   │   ├── 📄 AssignmentDao.kt         # Database access
        │   │   │   ├── 📄 Converters.kt            # Type converters
        │   │   │   └── 📄 StudyFlowDatabase.kt     # Room database
        │   │   │
        │   │   ├── 📁 model/
        │   │   │   └── 📄 Assignment.kt            # Data models
        │   │   │
        │   │   └── 📁 repository/
        │   │       └── 📄 AssignmentRepository.kt  # Repository layer
        │   │
        │   └── 📁 ui/
        │       ├── 📄 StudyFlowApp.kt              # Main app composable
        │       │
        │       ├── 📁 components/
        │       │   ├── 📄 AssignmentCard.kt        # Assignment card UI
        │       │   └── 📄 StatCard.kt              # Statistics card UI
        │       │
        │       ├── 📁 screens/
        │       │   ├── 📄 HomeScreen.kt            # Home dashboard
        │       │   ├── 📄 AllAssignmentsScreen.kt  # All assignments view
        │       │   ├── 📄 CalendarScreen.kt        # Calendar view
        │       │   ├── 📄 SettingsScreen.kt        # Settings screen
        │       │   └── 📄 AddAssignmentDialog.kt   # Add/edit dialog
        │       │
        │       ├── 📁 theme/
        │       │   ├── 📄 Color.kt                 # App colors
        │       │   ├── 📄 Theme.kt                 # Material theme
        │       │   └── 📄 Type.kt                  # Typography
        │       │
        │       └── 📁 viewmodel/
        │           └── 📄 MainViewModel.kt         # Main ViewModel
        │
        └── 📁 res/
            ├── 📁 drawable/
            │   └── 📄 ic_launcher_foreground.xml   # Launcher icon
            │
            ├── 📁 mipmap-*/                        # Launcher icons (all densities)
            │   ├── 📄 ic_launcher.xml
            │   └── 📄 ic_launcher_round.xml
            │
            ├── 📁 values/
            │   ├── 📄 colors.xml                   # Color resources
            │   ├── 📄 strings.xml                  # String resources
            │   └── 📄 themes.xml                   # Theme definitions
            │
            └── 📁 xml/
                ├── 📄 backup_rules.xml             # Backup configuration
                └── 📄 data_extraction_rules.xml    # Data extraction rules
```

## 🔧 Key Configuration Files

### build.gradle (app)
- **Plugins**: Android Application, Kotlin, Parcelize, KSP
- **SDK**: Compile 34, Min 24, Target 34
- **Dependencies**: Compose, Room, Navigation, Coroutines

### AndroidManifest.xml
- **Permissions**: Internet, Network State, Notifications, Alarms
- **Application**: StudyFlowApplication class
- **Activity**: MainActivity with launcher intent

## 🎯 Core Features Implemented

### ✅ Data Layer
- **Room Database** with Assignment entity
- **Type Converters** for Date, Priority, Lists
- **DAO** with comprehensive queries
- **Repository** pattern for data access

### ✅ UI Layer
- **Jetpack Compose** with Material Design 3
- **Navigation** between 4 main screens
- **State Management** with StateFlow
- **Responsive Design** for different screen sizes

### ✅ Business Logic
- **Priority Categorization** (High/Coming Up/Long Term)
- **Statistics Calculation** (Active/Completed/Overdue)
- **Search & Filtering** by subject, priority, status
- **CRUD Operations** for assignments

## 🚀 Build & Run Instructions

### Prerequisites Check
```bash
# Verify Java installation
java -version

# Should show JDK 11 or 17
```

### Build Commands
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

### Android Studio Setup
1. **Open Project**: File → Open → Select StudyFlow-Android folder
2. **Wait for Sync**: Gradle will download dependencies (~500MB)
3. **Create Emulator**: Tools → Device Manager → Create Device
4. **Run App**: Click green play button or Shift+F10

## 📱 App Navigation

```
🏠 Home Screen
├── Statistics Dashboard (Active/Completed/Overdue/Streak)
├── Progress Bar (Overall completion percentage)
├── High Priority Section (Due within 4 days)
├── Coming Up Section (Due within 1.5 weeks)
├── Long Term Section (Due in 3+ weeks)
└── Completed Section (Recently completed)

📋 All Assignments Screen
├── Search Bar (Title/Description/Subject search)
├── Filter Controls (Subject/Priority dropdowns)
├── Show Completed Toggle
└── Filtered Assignment List

📅 Calendar Screen
├── Month Navigation (Previous/Next buttons)
├── Calendar Grid (Clickable dates with indicators)
└── Selected Date Assignments List

⚙️ Settings Screen
├── Appearance (Dark Mode toggle)
├── Notifications (Enable/Reminder time)
├── Assignments (Default settings)
├── Data Management (Export/Import/Clear)
└── About (Version/Help/Rate)
```

## 🔄 Data Flow

```
UI Layer (Compose) 
    ↕️
ViewModel (StateFlow)
    ↕️
Repository (Suspend functions)
    ↕️
Room Database (SQLite)
```

## 🎨 Theming

- **Primary Color**: #667eea (StudyFlow Blue)
- **Secondary Color**: #764ba2 (StudyFlow Purple)
- **Accent Color**: #f59e0b (StudyFlow Orange)
- **Priority Colors**: Red (High), Orange (Medium), Green (Low)
- **Subject Colors**: Unique color for each subject type

## 📊 Statistics Tracking

- **Active Tasks**: Non-completed assignments
- **Completed**: Finished assignments
- **Overdue**: Past due date assignments
- **Completion Rate**: Percentage of completed tasks
- **Streak**: Consecutive days with completions (planned)

## 🔮 Future Enhancements

- [ ] Push notifications for due assignments
- [ ] Data export/import functionality
- [ ] Cloud synchronization
- [ ] Assignment templates
- [ ] Time tracking integration
- [ ] LMS platform integration
- [ ] Home screen widgets
- [ ] Advanced analytics dashboard
