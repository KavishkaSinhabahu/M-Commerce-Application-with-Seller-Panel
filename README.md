# Craftshub

Craftshub is an Android-based mobile commerce application developed as a final year undergraduate project. The system is designed to support a digital marketplace for handcrafted and local products by connecting buyers with independent sellers through a single mobile platform. The project focuses on usability, seller accessibility, and practical integration of modern mobile services such as cloud data storage, location services, and digital payments.

## Project Overview

The main objective of this project is to provide a mobile marketplace where users can browse products, search by category, manage a cart, place orders, and interact with seller information, while sellers can maintain their own product listings through a dedicated dashboard. The application was built as a practical academic solution that demonstrates mobile application development, cloud-backed data handling, and real-world feature integration.

## Key Features

- User registration and sign-in flow
- Customer and seller role handling
- Product browsing with category-based navigation
- Search and product detail views
- Shopping cart and checkout flow
- Order confirmation interface
- Seller dashboard for managing products
- Product image upload with Firebase Storage
- Store location viewing through Google Maps
- Integrated payment support with PayHere
- Animated and interactive mobile UI components

## Technology Stack

- Language: Java
- Platform: Android
- Build System: Gradle Kotlin DSL
- Minimum SDK: 24
- Target SDK: 35
- Cloud Services: Firebase Firestore, Firebase Storage, Firebase Authentication dependencies
- Maps and Location: Google Maps SDK, Google Play Services Location
- Networking and Utilities: Volley, OkHttp, Gson
- Media and UI Libraries: Glide, Lottie, MPAndroidChart, ImagePicker

## System Modules

### Customer Module
- Account access and session handling
- Home screen with recommended products and categories
- Product search and filtering by category
- Product detail, seller detail, and cart management
- Checkout, address entry, and order confirmation

### Seller Module
- Seller-specific dashboard access
- Add new product listings
- Upload product images
- View and manage seller products

### Supporting Services
- Firestore-based product and category retrieval
- Store location visualization using Google Maps
- Payment gateway integration
- Device interaction features such as proximity and shake-based services

## Project Structure

```text
mcommerce-app/
├── Craftshub/        # Main Android application
├── Craftshub-JIU/    # Additional project resources/module included in repository
└── README.md
```

The primary mobile application source code is located in `Craftshub/`.

## How to Run the Project

1. Clone this repository.
2. Open the `Craftshub/` folder in Android Studio.
3. Allow Gradle to sync and install the required dependencies.
4. Ensure `google-services.json` is available under `Craftshub/app/`.
5. Configure Firebase, Google Maps, and PayHere credentials for your own environment if needed.
6. Run the application on an Android emulator or a physical device.

## Configuration Notes

- Firebase services are used for product, category, user, and media management.
- Google Maps requires a valid API key.
- Payment processing requires valid PayHere merchant credentials.
- For production or public submission, sensitive keys and secrets should be removed from source control and replaced with secure environment-specific configuration.

## Academic Value

This project demonstrates practical knowledge in mobile application engineering, cloud database integration, user-centered interface design, and third-party service integration. It reflects the application of software engineering principles to a marketplace scenario with both customer-side and seller-side functionality.

## Future Improvements

- Secure authentication and password handling improvements
- Order tracking and delivery management
- In-app notifications
- Product reviews and ratings
- Admin dashboard and analytics
- Stronger offline support and caching
- Automated testing and CI/CD integration

## Author

Developed as a final year university project by Kavishka Sinhabahu.
