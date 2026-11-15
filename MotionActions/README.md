# Motion Actions (prototype)

A native Kotlin Android prototype targeting Samsung Galaxy M35 / Android 16 (API 36).

## Current prototype behaviour
- Double Chop: toggles the rear camera flashlight using CameraManager.
- Double Twist: posts a notification; tapping it opens the camera. Android may block direct background activity launches, so this is intentionally user-mediated.
- Runs sensor listeners in a user-started foreground service.

## Build
Open this folder in Android Studio with Android SDK Platform 36 installed, then Build > Build Bundle(s) / APK(s) > Build APK(s).

Or from a machine with Gradle installed:

```bash
gradle assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Important limitations
This is an early detector, not a validated Moto Actions clone. Thresholds are starting values and must be tuned with real sensor traces from the M35. Screen-off sensor delivery and foreground-service behaviour must be tested on the actual device and One UI build. Android 16 and Samsung battery management may affect reliability. The app requests CAMERA permission for torch access and notification permission for visible operation.
