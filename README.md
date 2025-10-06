# StudyFlow Android

A native Android version of the StudyFlow assignment tracking app, built with modern Android development practices.

## Features

- **Assignment Management**: Create, edit, and track assignments with priorities and due dates
- **Smart Organization**: Automatically categorizes assignments by urgency (High Priority, Coming Up, Long Term)
- **Statistics Dashboard**: Track your progress with completion rates and streaks
- **Calendar View**: Visual calendar showing all assignments by date
- **Search & Filter**: Find assignments quickly with powerful search and filtering
- **Modern UI**: Built with Jetpack Compose and Material Design 3
- **Offline First**: All data stored locally with Room database

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository pattern
- **Database**: Room (SQLite)
- **Navigation**: Navigation Compose
- **Dependency Injection**: Manual DI (easily upgradeable to Hilt)
- **Async**: Kotlin Coroutines & Flow
- **Material Design**: Material Design 3

## Project Structure

```
app/src/main/java/com/studyflow/tracker/
├── data/
│   ├── database/          # Room database, DAOs, converters
│   ├── model/            # Data models (Assignment, Priority, etc.)
│   └── repository/       # Repository layer
├── ui/
│   ├── components/       # Reusable UI components
│   ├── screens/         # Screen composables
│   ├── theme/           # App theming
│   └── viewmodel/       # ViewModels
├── MainActivity.kt      # Main activity
├── StudyFlowApp.kt     # Main app composable
└── StudyFlowApplication.kt # Application class
```

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 34
- Kotlin 1.9.10+

### Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd StudyFlow-Android
   ```

2. Open the project in Android Studio

3. Wait for Gradle sync to complete

4. Run the app on an emulator or physical device

### Building APK

To build a release APK:

1. Open terminal in the project root
2. Run: `./gradlew assembleRelease`
3. Find the APK in `app/build/outputs/apk/release/`

## Key Components

### Data Layer
- **Assignment**: Core data model with Room annotations
- **AssignmentDao**: Database access object with comprehensive queries
- **StudyFlowDatabase**: Room database configuration
- **AssignmentRepository**: Repository pattern implementation

### UI Layer
- **HomeScreen**: Main dashboard with priority-based assignment organization
- **AllAssignmentsScreen**: Complete list with search and filtering
- **CalendarScreen**: Calendar view with date-based assignment display
- **SettingsScreen**: App configuration and preferences
- **AddAssignmentDialog**: Assignment creation and editing

### ViewModels
- **MainViewModel**: Central state management for assignments and statistics

## Features in Detail

### Assignment Priorities
- **High Priority**: Due within 4 days
- **Coming Up**: Due within 1.5 weeks
- **Long Term**: Due in 3+ weeks

### Statistics
- Active tasks count
- Completed assignments
- Overdue assignments
- Completion streaks (planned)

### Subjects
Supports various subjects with emoji indicators:
- Math 📐
- Science 🔬
- English 📚
- History 🏛️
- Art 🎨
- Music 🎵
- PE ⚽
- Computer Science 💻
- Foreign Language 🌍
- Other 📝

## Customization

### Adding New Subjects
1. Add to `Subject` enum in `Assignment.kt`
2. Update subject emoji mapping in `AssignmentCard.kt`

### Modifying Priority Logic
- Update time ranges in `MainViewModel.kt`
- Adjust priority colors in `Color.kt`

### Theming
- Colors: `ui/theme/Color.kt`
- Typography: `ui/theme/Type.kt`
- Themes: `ui/theme/Theme.kt`

## Database Schema

### Assignment Table
- id (Primary Key)
- title, description
- subject, courseName
- dueDate, dueTime
- completed, completedAt
- priority, customColor
- source, createdAt
- estimatedHours, actualHours
- tags, attachments, notes

## Future Enhancements

- [ ] Notifications for due assignments
- [ ] Data export/import
- [ ] Cloud synchronization
- [ ] Assignment templates
- [ ] Time tracking
- [ ] Study session planning
- [ ] Integration with LMS platforms
- [ ] Widget support
- [ ] Dark mode toggle

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Based on the StudyFlow PWA
- Built with Android Jetpack libraries
- Uses Material Design 3 guidelines
