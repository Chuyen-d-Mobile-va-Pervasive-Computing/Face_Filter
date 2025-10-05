# ARFaceFilterDemo

Welcome to **ARFaceFilterDemo**, an Android application that applies augmented reality (AR) filters (e.g., sunglasses, cat ears, hat) to detected faces using the camera and Google ML Kit. This project demonstrates real-time face detection and overlay rendering with a user-friendly interface.

## Table of Contents
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the Project](#running-the-project)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## Features
- Real-time face detection using Google ML Kit.
- Apply AR filters (sunglasses, cat ears, hat) on detected faces.
- Switch between front and back camera.
- Capture photos with applied filters.
- Customizable filter selection via a user interface.

## Prerequisites
Before running the project, ensure you have the following:
- **Android Studio**: Version 2023.1.1 (Iguana) or later.
- **Android SDK**: API Level 26 (Android 8.0 Oreo) or higher.
- **Java Development Kit (JDK)**: Version 11.
- **Emulator or Physical Device**: With camera support and Android 8.0+.
- **Git**: For cloning the repository (optional).

## Installation

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/ARFaceFilterDemo.git
cd ARFaceFilterDemo
```

### 2. Open in Android Studio
- Launch Android Studio.
- Select **File > Open** and navigate to the cloned `ARFaceFilterDemo` directory.
- Wait for Gradle to sync and build the project.

### 3. Configure Local Properties
- Ensure the `local.properties` file exists in the project root (Android Studio usually generates this automatically).
- Verify the `sdk.dir` points to your Android SDK path, e.g.:
```
sdk.dir=C:\\Users\\YourUser\\AppData\\Local\\Android\\Sdk
```
- If missing, create it manually or let Android Studio generate it.

### 4. Install Dependencies
- Gradle will download required dependencies (e.g., CameraX, ML Kit) automatically during the sync process.

## Running the Project

### 1. Build the Project
- Click **Build > Make Project** in Android Studio to compile the code.
- Resolve any errors related to missing SDK components or dependencies.

### 2. Select a Device
- Connect a physical Android device with developer mode and USB debugging enabled.
- Alternatively, create an Android Virtual Device (AVD) via **Tools > Device Manager** with camera support.

### 3. Run the Application
- Click the green **Run** button (or **Shift + F10**) in Android Studio.
- Grant camera permissions when prompted by the app.

### 4. Troubleshooting
- If the app crashes, check the **Logcat** window in Android Studio for errors.
- Ensure all drawable resources (`cat_ears.png`, `hat.png`, `sunglasses.png`) are present in `app/src/main/res/drawable/`.

## Usage
| **Action**           | **Description**                                                                 |
|----------------------|---------------------------------------------------------------------------------|
| **Select a Filter**  | Use the filter selector bar at the top to choose "None", "Sunglasses", "Cat Ears", or "Hat". |
| **Switch Camera**    | Tap the flip camera button (left bottom) to toggle between front and back cameras. |
| **Capture Photo**    | Tap the capture button (center bottom) to save a photo with the applied filter to your device's cache directory. |
| **Debug Info**       | The top-right text displays the number of detected faces.                        |

## Project Structure
```
ARFaceFilterDemo/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/arfacefilterdemo/
│   │   │   │   ├── ARCameraActivity.kt       # Main activity for camera and filter logic
│   │   │   │   ├── FilterOverlayView.kt      # Custom view for rendering filters on Canvas
│   │   │   │   ├── FilterSelectorView.kt     # Filter selection UI
│   │   │   │   └── MainActivity.kt           # Entry point activity (redirects to ARCameraActivity)
│   │   │   ├── res/
│   │   │   │   ├── drawable/                # Filter images and button icons
│   │   │   │   └── layout/                  # XML layouts (activity_ar_camera.xml)
│   │   │   └── AndroidManifest.xml          # App manifest
│   ├── build.gradle.kts                    # App-level Gradle configuration
├── build.gradle.kts                        # Project-level Gradle configuration
├── gradle/
├── .gitignore                              # Git ignore file
└── README.md                               # This file
```

## Contributing
Contributions are welcome! Please follow these steps:
1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m 'Add your feature'`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a pull request.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contact
For questions or feedback, reach out to [yourusername@example.com](mailto:yourusername@example.com) or open an issue on the [GitHub repository](https://github.com/yourusername/ARFaceFilterDemo).