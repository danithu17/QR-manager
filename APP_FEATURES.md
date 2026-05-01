# QR Inventory & Sales Manager

A high-performance, premium Android application built with Java, featuring a modern **Glassmorphic** design and robust inventory management capabilities.

## ✨ Core Features

### 1. Premium Glassmorphic UI/UX
*   **iOS 18/macOS Aesthetics:** Deep blue and dark gray background with semi-transparent frosted glass cards.
*   **Material 3 Components:** Utilizes the latest Material Design components with custom 24dp+ rounded corners and subtle shadows.
*   **Smooth Animations:** Elegant "Fade-in" animations for list items and smooth transitions between states.
*   **Responsive Dashboard:** A high-level view of business health using glassmorphic statistics cards.

### 2. QR Inventory Management
*   **QR Upload & Import:** Ability to upload or import QR code images directly from the device gallery.
*   **Categorization:** Assign specific categories (e.g., Electronics, Fashion, etc.) to each QR code.
*   **Status Tracking:** Real-time tracking of item statuses:
    *   🟢 **Available:** Ready for sale or reservation.
    *   🟡 **Pending:** Awaiting further action.
    *   🔴 **Sold:** Successfully transacted.
    *   🟣 **Pre-ordered:** Reserved for specific customers.

### 3. Sales & Reservation Workflow
*   **One-Tap Sell:** Move items from "Available" to "Sold" with a single click, instantly updating inventory counts.
*   **Reservation System:** Mark items as "Pre-ordered" to set them aside for customers, moving them to a dedicated Pre-order view.
*   **Smart Filtering:** Bottom navigation allows quick switching between Total Inventory, Pre-orders, and Sales history.

### 4. Customer & Order Management
*   **Customer Records:** Store names and contact details (WhatsApp/Phone) for every buyer or reservation.
*   **Direct Messaging:** Instant WhatsApp messaging button to notify customers when their pre-ordered items are ready.
*   **Linked History:** View customer details directly in the inventory list.

### 5. Enhanced Security
*   **Biometric Lock:** Secure access using device Biometrics (Fingerprint or Face ID) on startup.
*   **Professional Reporting:** Generate high-quality PDF sales reports with a single tap for tax or business analysis.
*   **Data Integrity:** Validated entry fields to ensure customer data accuracy.

### 6. Cloud Sync & Multi-device Support
*   **Firebase Integration:** Real-time synchronization of inventory data to the cloud for multi-device access.
*   **Automated Backups:** Daily cloud backups to Firebase to prevent data loss from device failure.
*   **Offline First:** Works seamlessly offline using Room Database and syncs when connectivity is restored.

## 🛠️ Technical Stack
*   **Language:** Java
*   **UI Framework:** Android Material 3 (XML)
*   **Database:** Room Persistence Library (Local) & Firebase Realtime Database (Cloud)
*   **Security:** AndroidX Biometric API
*   **Image Loading:** Glide
*   **CI/CD:** GitHub Actions (Ubuntu runner, JDK 17)

## 📱 Navigation Structure
*   **Home/Dashboard:** Statistics overview and recent activity.
*   **Inventory:** Manage available QR codes and initiate sales/reservations.
*   **Pre-orders:** View and manage reserved items.
*   **Sales:** Review historical transaction data.
*   **Floating Action Button (FAB):** Global entry point for adding new inventory.
