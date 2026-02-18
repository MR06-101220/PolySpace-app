# PolySpace-app
**The all-in-one academic hub to simplify your student life in Polytech Paris-Saclay.**

PolySpace centralizes everything you need for your daily studies—Timetable, Homework, and Grades—into a single, sleek Android application. No more jumping between three different portals.

---

## Key Features

* **Unified Timetable:** Real-time schedule integration powered by a custom REST API.
* **Homework Tracker:** Manage your assignments locally with a robust **Room database** for offline access.
* **Grade Monitoring:** Automatic synchronization of academic results through advanced **HTML scraping**.
* **Modern UI:** Built with **Jetpack Compose** for a smooth and reactive user experience.

##  Technical Stack & Architecture

PolySpace is built with modern Android standards to ensure maintainability and performance:

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose
* **Local Persistence:** Room Database (for Homework)
* **Data Fetching:** Custom API Integration (Retrofit/OkHttp)
    * HTML Scraping for Grades (Jsoup)
* **Architecture:** MVVM (Model-View-ViewModel) with a **Repository Pattern** to decouple data sources from the UI.



---

##  Getting Started

### Prerequisites
* Android Studio Ladybug or newer
* JDK 17+
* Android SDK 34+

### Installation
1.  **Clone the repository**:
    ```bash
    git clone git@github.com:MR06-101220/PolySpace-app.git
    ```
2.  **Open in Android Studio** and let Gradle sync.
3.  **Run on your device/emulator.**

---

## Screenshots
| Timetable | Homework | Grades |
| :---: | :---: | :---: |
| ![Timetable](./screenshots/Timetable.jpg) | ![Homework](./screenshots/Homeworks.jpg) | ![Grades](./screenshots/Grades.jpg) |

---

## Disclaimer & Credits
* **Data Privacy:** All scraped data and homework are stored locally on the device.
* **API:** Timetable data is provided by https://github.com/lekawik custom API.
* **Note:** Since the grading system relies on HTML scraping, it may require updates if the source portal changes its structure.
