# 🪞 Real Mirror

**See yourself as others do.**

Real Mirror is a privacy-focused, open-source Android app that shows your **true, non-reversed reflection** — the way others actually see you. Unlike your bathroom mirror, Real Mirror flips the camera feed so you can see your real appearance. Toggle between **Mirror** and **Real** modes instantly, capture high-quality photos, and record videos in real time.

---

## ✨ Features

- **Mirror / Real Toggle** — Switch between the traditional mirrored view and the true (non-mirrored) view with a single tap
- **Photo Capture** — Take high-resolution photos saved directly to your gallery
- **Video Recording** — Record videos with audio, complete with a live timer indicator
- **Brightness Control** — Adjust screen brightness on the fly with a sleek vertical slider
- **Full-Screen Viewfinder** — Edge-to-edge immersive camera preview
- **Privacy First** — No data collection, no analytics, no internet required for core functionality. Your camera feed never leaves your device
- **Modern UI** — Glassmorphism-inspired dark theme built entirely with Jetpack Compose
- **Check for Updates** — Built-in update checker via the GitHub Releases API

---

## 📱 Screenshots

<!-- Add your screenshots here -->
<!-- ![Mirror Mode](screenshots/mirror_mode.png) -->
<!-- ![Real Mode](screenshots/real_mode.png) -->
<!-- ![About Screen](screenshots/about_screen.png) -->

*Screenshots coming soon*

---

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| **Kotlin** | Primary language |
| **Jetpack Compose** | Declarative UI framework |
| **Material 3** | Design system & theming |
| **CameraX** | Camera preview, photo capture & video recording |
| **AndroidX Lifecycle** | Lifecycle-aware components & ViewModels |
| **GitHub API** | In-app update checking via public releases endpoint |

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK 11** or higher
- Android device or emulator running **API 26+** (Android 8.0 Oreo)

### Build & Run

```bash
# 1. Clone the repository
git clone https://github.com/abzgif/Android-Real-Mirror.git

# 2. Open in Android Studio
#    File → Open → select the cloned directory

# 3. Sync Gradle & Run
#    Click the ▶ Run button or use Shift+F10
```

> **Note:** The app requires a physical front camera for full functionality. The camera preview may not work on some emulators.

---

## 🔒 Permissions

| Permission | Why it's needed |
|---|---|
| `CAMERA` | Access the front camera for the mirror viewfinder |
| `RECORD_AUDIO` | Record audio during video capture |
| `READ_MEDIA_IMAGES` / `READ_MEDIA_VIDEO` | Access gallery on Android 13+ |
| `READ/WRITE_EXTERNAL_STORAGE` | Save photos/videos on older Android versions |
| `INTERNET` | Check for app updates via GitHub API (optional) |

All permissions are requested at runtime with clear explanations. The app works entirely offline except for the optional update check.

---

## 📁 Project Structure

```
app/src/main/java/com/realmirror/app/
├── MainActivity.kt              # Entry point, permission handling, brightness control
├── camera/
│   └── CameraManager.kt         # CameraX setup, photo capture, video recording
├── update/
│   └── UpdateChecker.kt         # GitHub Releases API update checker
├── viewmodel/
│   └── CameraViewModel.kt       # UI state management for camera operations
└── ui/
    ├── theme/
    │   └── RealMirrorTheme.kt    # Dark glassmorphism color scheme & theme
    ├── screens/
    │   ├── MainScreen.kt         # Root navigation between screens
    │   ├── PermissionScreen.kt   # Permission request screen
    │   └── AboutScreen.kt        # App info & update checker
    └── components/
        ├── MirrorRealToggle.kt   # Animated Mirror/Real pill toggle
        ├── CaptureButtons.kt     # Shutter & record buttons with animations
        ├── BrightnessSlider.kt   # Custom vertical brightness slider
        └── RecordingIndicator.kt # Pulsing recording indicator with timer
```

---

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Guidelines

- Follow existing code style and architecture patterns
- Write meaningful commit messages
- Test on a physical device before submitting

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

---

## 🙏 Acknowledgments

- [CameraX](https://developer.android.com/training/camerax) — Google's Jetpack camera library
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — Android's modern toolkit for building native UI
- [Material 3](https://m3.material.io/) — Google's design system

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/abzgif">abzgif</a>
</p>
