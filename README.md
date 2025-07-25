# Book Haven - Android Book Shopping App

Book Haven is a native Android application designed for book lovers to browse, search, and purchase books. It offers a seamless mobile shopping experience, from discovering new titles to a secure checkout process.

## 🚀 Features

- **User Authentication:** Secure sign-up and login functionality.
- **Book Discovery:**
    - Browse featured books and new arrivals on the home screen.
    - Explore books by category.
    - Powerful search functionality to find specific books.
- **Product Details:** View detailed information for each book, including description, author, and price.
- **Shopping Cart:** Add and manage books in a personal shopping cart.
- **Checkout & Payment:**
    - Smooth and intuitive checkout process.
    - Integration with a payment gateway for secure transactions.
    - Apply vouchers for discounts.
- **Order Management:** View order history and details of past purchases.
- **User Profile:**
    - View and edit user profile information.
    - Change account password.

## 🛠️ Tech Stack

- **Platform:** Android
- **Language:** Java
- **Build Tool:** Gradle
- **Architecture:** MVVM (Model-View-ViewModel) pattern
- **UI:** Android XML Layouts
- **Networking:** Retrofit for consuming RESTful APIs
- **Image Loading:** Glide/Picasso (or similar, to be confirmed by inspecting dependencies)

## 🔌 Backend API

**Important:** This repository contains the **Android client application only**. The server-side code is not included. For the app to function fully, it must be connected to a backend API that provides the necessary endpoints for books, users, orders, etc.

The Retrofit service interfaces for interacting with this API are defined in the `app/src/main/java/com/son/bookhaven/services/` directory. You will need to configure the base URL in the `ApiClient` to point to your backend server.

## ⚙️ Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

- Android Studio IDE
- Android SDK
- Java Development Kit (JDK)

### Installation

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/your_username/book-haven.git
    ```
2.  **Open in Android Studio:**
    - Launch Android Studio.
    - Select `File > Open` and navigate to the cloned project directory.
3.  **Build the project:**
    - Android Studio will automatically sync the project with Gradle.
    - To build the project manually, go to `Build > Make Project`.
4.  **Run the application:**
    - Select a target device (emulator or physical device).
    - Click the `Run 'app'` button.

## 📂 Project Structure

The project is organized into the following main directories:

```
app/src/main/
├── java/com/son/bookhaven/
│   ├── data/                 # Contains adapters, models (DTOs), and repositories
│   ├── services/             # API service interfaces for Retrofit
│   ├── ui/                   # Activities and Fragments (the user interface)
│   └── utils/                # Utility classes like ApiClient, TokenManager
├── res/
│   ├── layout/               # XML layout files for activities and fragments
│   ├── drawable/             # Image assets
│   ├── values/               # String, color, and style resources
│   └── navigation/           # Navigation graphs
└── AndroidManifest.xml       # Application manifest
```
