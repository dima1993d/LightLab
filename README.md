# LightLab: AMOLED Electrical Sensing Probe

This Android app performs a **read-only inspection** of device/display metadata and accessible Linux display-related device paths. It also provides a white/black screen area for controlled illumination, but **does not claim to detect incoming light through the display**.

There is no standard Android API for reading OLED pixel photocurrent. Merely discovering a readable display device path is not evidence of photocurrent sensing; interpreting proprietary driver diagnostics requires kernel/display-driver research, potentially root and dedicated hardware.

The app does not use the ambient-light sensor, camera, or touchscreen as a measurement source. It does not write to driver interfaces.

## Download

Open [GitHub Actions](../../actions), select the latest successful **Build Android APK** run, download `S22LightLab-debug-apk`, extract the ZIP and install `app-debug.apk`.
