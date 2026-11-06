# LightLab for Samsung Galaxy S22 Ultra

Android experiment showing a large white test area, ambient-light lux readings, baseline calibration and independently toggled touchscreen detection.

**This does not measure incoming light through AMOLED pixels.** It reads the phone's dedicated ambient-light sensor.

## Install APK using phone
Open [Actions](../../actions), choose the latest successful **Build Android APK** run, download the **S22LightLab-debug-apk** artifact, extract the ZIP, and install `app-debug.apk`.

## Build
JDK 17, Android SDK 35, Gradle 8.10.2: `gradle assembleDebug`.
