# StudyFlow Android - Troubleshooting Guide

## 🚨 Common Issues & Solutions

### 1. Gradle Sync Failed

**Problem**: "Gradle sync failed" error in Android Studio

**Solutions**:
```bash
# Option A: Clean and rebuild
./gradlew clean
./gradlew build

# Option B: Invalidate caches
# In Android Studio: File → Invalidate Caches and Restart

# Option C: Check internet connection
# Gradle needs to download dependencies (~500MB first time)
```

**Check**:
- ✅ Internet connection active
- ✅ Android Studio version 2023.1.1+
- ✅ JDK 11 or 17 installed

### 2. SDK Not Found

**Problem**: "Android SDK not found" or "compileSdk 34 not found"

**Solution**:
1. Open Android Studio
2. Go to **Tools → SDK Manager**
3. Install **Android 14 (API 34)**
4. Install **Android SDK Build-Tools 34.0.0**
5. Click **Apply** and wait for download

### 3. KSP Plugin Issues

**Problem**: "Plugin [id: 'com.google.devtools.ksp'] was not found"

**Solution**:
```gradle
// In app/build.gradle, ensure this line exists:
id 'com.google.devtools.ksp' version '1.8.10-1.0.9'
```

### 4. Room Database Compilation Errors

**Problem**: Room annotation processor errors

**Check**:
```gradle
// Ensure these dependencies exist in app/build.gradle:
implementation 'androidx.room:room-runtime:2.5.2'
implementation 'androidx.room:room-ktx:2.5.2'
ksp 'androidx.room:room-compiler:2.5.2'
```

### 5. Compose Version Conflicts

**Problem**: "Compose compiler version mismatch"

**Solution**:
```gradle
// In app/build.gradle, ensure matching versions:
composeOptions {
    kotlinCompilerExtensionVersion '1.4.6'
}

// And in dependencies:
implementation platform('androidx.compose:compose-bom:2023.06.01')
```

### 6. App Crashes on Launch

**Problem**: App builds but crashes immediately

**Check**:
1. **Emulator/Device API Level**: Must be API 24+ (Android 7.0+)
2. **Permissions**: Check AndroidManifest.xml has required permissions
3. **Logcat**: View crash logs in Android Studio Logcat

**Common Crash Fixes**:
```kotlin
// If MainActivity crashes, check StudyFlowApplication exists:
class StudyFlowApplication : Application() {
    val database by lazy { StudyFlowDatabase.getDatabase(this) }
}

// Ensure it's declared in AndroidManifest.xml:
<application android:name=".StudyFlowApplication" ...>
```

### 7. No Sample Data Showing

**Problem**: App runs but shows empty screens

**Solution**:
```kotlin
// In MainActivity.onCreate(), ensure this line exists:
viewModel.addSampleAssignments()
```

### 8. Navigation Not Working

**Problem**: Bottom navigation doesn't switch screens

**Check**:
```kotlin
// Ensure all screens are properly defined in StudyFlowApp.kt:
composable(Screen.Home.route) { HomeScreen(viewModel = viewModel) }
composable(Screen.AllAssignments.route) { AllAssignmentsScreen(viewModel = viewModel) }
composable(Screen.Calendar.route) { CalendarScreen(viewModel = viewModel) }
composable(Screen.Settings.route) { SettingsScreen() }
```

## 🔧 Build Environment Setup

### Required Software Versions
- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: 11 or 17 (bundled with Android Studio)
- **Gradle**: 8.0 (handled by wrapper)
- **Android Gradle Plugin**: 8.1.2
- **Kotlin**: 1.8.10

### SDK Requirements
- **Compile SDK**: 34 (Android 14)
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 24 (Android 7.0)

### Emulator Recommendations
- **Device**: Pixel 6 or Pixel 7
- **API Level**: 34 (Android 14)
- **Target**: Google APIs (not Google Play)
- **RAM**: 2GB minimum, 4GB recommended

## 📱 Testing Checklist

### Before Running
- [ ] Gradle sync completed successfully
- [ ] No red errors in code editor
- [ ] Emulator/device connected and recognized
- [ ] Internet connection available (first run)

### After Launch
- [ ] App icon appears on device
- [ ] App launches without crashes
- [ ] Sample assignments visible on Home screen
- [ ] Bottom navigation works
- [ ] FAB (+ button) opens Add Assignment dialog
- [ ] All 4 screens accessible

### Core Functionality Test
- [ ] Create new assignment via FAB
- [ ] Toggle assignment completion
- [ ] Delete assignment
- [ ] Search assignments in All tab
- [ ] Filter by subject/priority
- [ ] Calendar shows assignments by date
- [ ] Settings screen opens

## 🐛 Debug Tools

### Logcat Filtering
```
# Filter by app package:
package:com.studyflow.tracker

# Filter by error level:
level:error

# Filter by tag:
tag:StudyFlow
```

### Common Log Tags to Watch
- `StudyFlowDatabase`: Database operations
- `MainViewModel`: ViewModel state changes
- `AssignmentRepository`: Data operations
- `Compose`: UI rendering issues

### Performance Monitoring
- **Memory**: Tools → Profiler → Memory
- **CPU**: Tools → Profiler → CPU
- **Network**: Tools → Profiler → Network

## 🔄 Reset Instructions

### Clean Build
```bash
# Stop Gradle daemon
./gradlew --stop

# Clean project
./gradlew clean

# Rebuild
./gradlew build
```

### Reset Android Studio
1. **File → Invalidate Caches and Restart**
2. **Choose "Invalidate and Restart"**
3. **Wait for re-indexing to complete**

### Reset Emulator
1. **Tools → Device Manager**
2. **Click dropdown next to emulator**
3. **Select "Cold Boot Now"**

### Nuclear Option (Complete Reset)
1. Close Android Studio
2. Delete `.gradle` folder in project root
3. Delete `build` folders in app and root
4. Delete `.idea` folder
5. Reopen project in Android Studio
6. Wait for complete re-sync

## 📞 Getting Help

### Error Message Patterns
- **"Could not resolve"**: Dependency/network issue
- **"Unresolved reference"**: Import/classpath issue  
- **"Type mismatch"**: Kotlin type error
- **"No such file"**: Missing resource/file

### Useful Resources
- [Android Developer Documentation](https://developer.android.com/docs)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)

### Community Support
- [Stack Overflow - Android](https://stackoverflow.com/questions/tagged/android)
- [Reddit - AndroidDev](https://www.reddit.com/r/androiddev/)
- [Android Developers Discord](https://discord.gg/android-developers)
