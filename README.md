# AdFlow Lab — Live UI Observer

Android Kotlin project for read-only accessibility observation of visible labels, bounds, screen changes, countdowns, and displayed reward progress. It does not click third-party controls or automate rewarded-ad completion.

## Build with GitHub Actions
Open Actions > Build Android APK > Run workflow. Download the AdFlowLab-LiveObserver-debug-apk artifact and extract app-debug.apk. Install on Android, then enable the app's Accessibility Service in Settings.

## Use
Open AdFlow Lab, tap Start monitoring, then switch to Pocket FM. Return to the app to inspect or share logs. Stop monitoring when finished.

## Privacy and limitations
Accessibility content may include sensitive screen text. Review logs before sharing. Accessibility nodes may not expose video or WebView controls; this version does not include screenshot OCR. No automated taps are performed.
