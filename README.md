# Proximity Attendance System 📍📱

> A secure, location-based Android application designed to eliminate proxy attendance in university classrooms using smart geofencing and unique device checks.

---

## 📖 Overview

Traditional paper-based attendance is time-consuming, and simple digital solutions (like QR codes or forms) are easily exploited for "proxy" attendance.

The **Proximity Attendance System** solves this by ensuring physical presence. It uses the **Haversine Formula** to verify that a student is within a **50-meter radius** of the professor before accepting their attendance.

---

## ✨ Key Features

### 🛡️ Security & Anti-Proxy
* **Smart Geofencing:** Attendance is rejected if the distance between the student and professor exceeds 50 meters.
* **Device Lock:** Captures the unique `ANDROID_ID`. A single device can only mark attendance once per session.
* **Domain-Locked Login:** Integrated with Google Sign-In, restricted specifically to the university domain.

### 👨‍🏫 For Professors
* **Instant Sessions:** Start a class in seconds, generating a secure, temporary 4-digit code.
* **Real-time Location Fetching:** Automatically captures the classroom's center point using GPS.
* **One-Click Export:** Instantly download the attendance list as a **CSV file** at the end of the class.

### 👨‍🎓 For Students
* **Live Dashboard:** View active classes in real-time.
* **One-Tap Marking:** Simple interface to enter the code and mark presence.
* **Real-time Feedback:** Instant confirmation of success or failure (e.g., "Too far from class").

---

## 🏗️ Architecture & How it Works

The system follows a **Client-Server Architecture**.

1.  **Authentication:** Users log in via Google on the Android App. The app determines their role (Professor/Student) based on their email address.
2.  **Session Start:** The Professor app fetches high-accuracy GPS coordinates and sends them to the Spring Boot Server along with the subject name.
3.  **Marking Attendance:** The Student app fetches their own GPS coordinates and unique Device ID. It sends this data along with the session code to the server.
4.  **Validation Logic (Server-Side):**
    * Is the 4-digit code correct?
    * Is `Distance(Prof_GPS, Student_GPS) <= 50m`?
    * Has this `Device_ID` already marked attendance for this session?
5.  **Result:** If all checks pass, data is saved to the H2 Database and synced with Firebase.

---

## 🛠️ Tech Stack

| Component | Technology Used |
| :--- | :--- |
| **Frontend Mobile App** | Android Native (Java), XML Layouts |
| **Backend API** | Java Spring Boot (REST API) |
| **Primary Database** | H2 Database (In-Memory for speed during demo) |
| **Secondary Storage** | Firebase Realtime Database |
| **Authentication** | Google Sign-In (OAuth 2.0) + Firebase Auth |
| **Location Provider** | Google Fused Location Provider API |
| **Build Tools** | Gradle (Android), Maven (Backend) |

---

## ⚙️ Installation & Setup Guide

### Prerequisites
* Java Development Kit (JDK) 17 or higher.
* Android Studio (for the mobile app).
* VS Code or IntelliJ IDEA (for the backend server).
* An Android physical device (highly recommended for accurate GPS testing).

### Step 1: Backend Setup (Spring Boot)
1.  Open the backend project folder in your IDE.
2.  Let Maven resolve dependencies.
3.  Run the main application file (`DemoApplication.java`).
4.  The server will start on `localhost:8080`.
    > **Important:** Find your laptop's local IP address (e.g., run `ipconfig` in terminal). You will need this for the Android app.

### Step 2: Frontend Setup (Android)
1.  Open the Android project folder in Android Studio.
2.  **Crucial Configuration:** You must point the app to your local server's IP address.
    * Open `ProfessorActivity.java` and update `BASE_URL`:
        `private static final String BASE_URL = "http://YOUR_LAPTOP_IP:8080/api";`
    * Open `DashboardActivity.java` and update `BASE_URL`:
        `private static final String BASE_URL = "http://YOUR_LAPTOP_IP:8080/api";`
3.  Ensure the `google-services.json` file is present in the `app/` folder (required for Firebase/Google Login).
4.  Build and run the app on your physical Android device.

---

## 📱 Usage

### Professor Flow
1.  Log in with a professor account.
2.  Enter the Subject Name (e.g., "Data Structures").
3.  Click **"Start Session"** (Wait for GPS fetch).
4.  Share the displayed 4-digit code with students physically present in the class.
5.  Once done, click **"Stop Session"**.
6.  Click **"Export CSV"** to download the attendance sheet.

### Student Flow
1.  Log in with a student account.
2.  Click **"Refresh List"** to see active classes.
3.  Tap on the class name.
4.  Enter the 4-digit code provided by the professor.
5.  Click **"Mark Present"**. The app will verify your location and device ID.

---

## 🔮 Future Scope

* **Biometric Verification:** Integrate **Fingerprint or Face ID** before the "Mark Present" button is activated for an added layer of security.
* **Dynamic Radius:** Allow professors to set a custom radius (e.g., 20m for labs, 100m for auditoriums) via a slider.
* **Offline Mode:** Allow students to mark attendance without internet, storing data locally and syncing when connectivity returns.

---

## 📝 License

This project is developed for academic purposes.
