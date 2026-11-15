# Build without Android Studio (GitHub Actions)

This project includes `.github/workflows/build-apk.yml` for a cloud build.

1. Create a new GitHub repository (private is fine).
2. Upload the **contents** of this `MotionActions` folder to the repository root (so `app/`, `build.gradle.kts`, and `.github/` are at the root).
3. Open the repository's **Actions** tab.
4. Select **Build MotionActions APK** → **Run workflow**.
5. Wait for the run to finish successfully.
6. Open that run and download the **MotionActions-debug-apk** artifact.
7. Extract the downloaded artifact ZIP; it contains `app-debug.apk`.
8. Transfer the APK to the Galaxy M35 and install it. Android may ask you to allow installs from the browser or file manager used to open it.

This produces a debug APK for testing, not a Play Store release. The workflow runs Gradle and Android SDK tools on GitHub's hosted runner; Android Studio is not needed locally.
