# 🥬 Fresh Save

**Fresh Save** is an Android application designed to help users manage their food inventory, track expiration dates, and reduce food waste. The app provides an intuitive interface to add, categorize, and monitor food items, with smart notifications for items expiring soon.

## 📱 Features

- **Food Inventory Management**: Add, edit, and delete food items with details like quantity, category, and expiration dates
- **Expiration Tracking**: Monitor items that are expiring soon (within 7 days)
- **Category Organization**: Organize food items by categories (Fruits, Vegetables, Dairy, etc.)
- **Favorites System**: Mark frequently used items as favorites for quick access
- **Statistics Dashboard**: View insights about your food inventory and potential savings
- **Recipe Suggestions**: Get recipe recommendations based on your inventory (placeholder feature)
- **Donation Feature**: Track food donations and reduce waste
- **Modern UI**: Clean, Material Design 3 interface built with Jetpack Compose

## 🛠️ Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Appwrite (Backend-as-a-Service)
- **Dependency Injection**: Koin
- **Navigation**: Jetpack Navigation Compose
- **Minimum SDK**: Android 10 (API 29)
- **Target SDK**: Android 14 (API 35)

## 🏗️ Project Structure

```
app/src/main/java/com/bhaswat/freshsave/
├── data/
│   └── remote/
│       └── AppwriteInventoryRepository.kt    # Appwrite database operations
├── di/
│   └── AppModule.kt                          # Dependency injection setup
├── model/
│   └── InventoryItem.kt                      # Data models
├── repository/
│   └── InventoryRepository.kt                # Repository interface
├── ui/
│   ├── HomeScreen.kt                         # Main inventory screen
│   ├── AddItemScreen.kt                      # Add/edit item screen
│   ├── StatsScreen.kt                        # Statistics screen
│   ├── RecipesScreen.kt                      # Recipe suggestions
│   ├── DonateScreen.kt                       # Donation tracking
│   ├── navigation/                           # Navigation setup
│   └── theme/                                # App theming
├── viewmodel/
│   └── HomeViewModel.kt                      # Business logic
├── FreshSaveApp.kt                           # Application class
└── MainActivity.kt                           # Main activity
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog | 2023.1.1 or later
- Android SDK with API level 29 or higher
- Kotlin 1.9.0 or later
- JDK 11 or later

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/THORzero9/Fresh_Save.git
   cd Fresh_Save
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Click "Open an existing project"
   - Navigate to the cloned repository folder
   - Select the project root directory

3. **Configure Appwrite Backend**
   
   ⚠️ **Important**: You need to set up your own Appwrite instance for this app to work.
   
   - Create an [Appwrite account](https://appwrite.io/) or set up a self-hosted instance
   - Create a new project (choose any project ID you prefer)
   - Create a database (choose any database name)
   - Create a collection for inventory items with the following attributes:
     - `name` (String, required)
     - `category` (String, required)
     - `quantity` (Float, required)
     - `unit` (String, optional)
     - `expiryDate` (DateTime, optional)
     - `isFavorite` (Boolean, default: false)
   - Note down your project ID, database ID, and collection ID

4. **Update App Configuration**
   - Open `app/src/main/java/com/bhaswat/freshsave/FreshSaveApp.kt`
   - Replace the configuration with your Appwrite details:
   ```kotlin
   client = Client(this)
       .setEndpoint("YOUR_APPWRITE_ENDPOINT")    // e.g., "https://cloud.appwrite.io/v1"
       .setProject("YOUR_PROJECT_ID")            // Your Appwrite project ID
   ```
   
   - Open `app/src/main/java/com/bhaswat/freshsave/data/remote/AppwriteInventoryRepository.kt`
   - Update the database and collection IDs:
   ```kotlin
   private val databaseId = "YOUR_DATABASE_ID"
   private val collectionId = "YOUR_COLLECTION_ID"
   ```

5. **Build and Run**
   - Sync the project with Gradle files
   - Build the project (Build → Make Project)
   - Run on an emulator or physical device (API 29+)

## 🎯 Usage

### Adding Food Items
1. Open the app and navigate to the Home screen
2. Tap the "+" button to add a new item
3. Fill in the item details:
   - Name (required)
   - Category (required)
   - Quantity and unit
   - Expiration date
   - Mark as favorite (optional)
4. Tap "Save" to add the item to your inventory

### Managing Inventory
- **View All Items**: Browse your complete food inventory on the Home screen
- **Filter by Category**: Use the category filter to view specific types of food
- **Edit Items**: Tap on any item to edit its details
- **Delete Items**: Long press or use the delete option to remove items
- **Track Expiring Items**: View items expiring within 7 days in a dedicated section

### Navigation
The app features a bottom navigation bar with five main sections:
- **Home**: Main inventory management
- **Stats**: View inventory statistics and savings
- **Recipes**: Get recipe suggestions (coming soon)
- **Donate**: Track food donations and waste reduction

## 🔧 Configuration

### Database Schema
The app uses Appwrite as the backend database. The main collection schema includes:

```json
{
  "name": "string (required)",
  "category": "string (required)", 
  "quantity": "number (required)",
  "unit": "string (optional)",
  "expiryDate": "datetime (optional)",
  "isFavorite": "boolean (default: false)"
}
```

### Environment Variables (Recommended)
For better security, consider using environment variables or a configuration file for sensitive data:

```kotlin
// Example: Using BuildConfig for configuration
object AppConfig {
    const val APPWRITE_ENDPOINT = BuildConfig.APPWRITE_ENDPOINT
    const val APPWRITE_PROJECT_ID = BuildConfig.APPWRITE_PROJECT_ID
    const val DATABASE_ID = BuildConfig.DATABASE_ID
    const val COLLECTION_ID = BuildConfig.COLLECTION_ID
}
```

### Dependencies
Key dependencies used in the project:

```kotlin
// Core Android libraries
implementation("androidx.core:core-ktx")
implementation("androidx.lifecycle:lifecycle-runtime-ktx")
implementation("androidx.activity:activity-compose")

// Jetpack Compose
implementation(platform("androidx.compose:compose-bom"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")

// Appwrite SDK
implementation("io.appwrite:sdk-for-android")

// Dependency Injection
implementation("io.insert-koin:koin-androidx-compose")
```

## 🔒 Security Considerations

- **Never commit sensitive data**: Ensure Appwrite credentials are not hardcoded in your repository
- **Use environment variables**: Store configuration in build variants or environment files
- **API Keys**: Keep your Appwrite project keys secure and rotate them regularly
- **Database permissions**: Configure appropriate read/write permissions in your Appwrite console

## 🤝 Contributing

Contributions are welcome! Here's how you can help:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Use meaningful commit messages
- Add comments for complex logic
- Test your changes thoroughly
- Update documentation when necessary
- **Never commit sensitive configuration data**

## 📋 Roadmap

### Current Features (v1.0)
- ✅ Basic inventory management
- ✅ Expiration date tracking
- ✅ Category organization
- ✅ Appwrite integration
- ✅ Material Design 3 UI

### Planned Features (Future Versions)
- 🔄 Recipe suggestions based on inventory
- 🔄 Barcode scanning for easy item addition
- 🔄 Nutrition information integration
- 🔄 Shopping list generation
- 🔄 Food waste analytics
- 🔄 Social sharing features
- 🔄 Offline mode support
- 🔄 Multiple language support
- 🔄 Enhanced security features

## 🐛 Known Issues

- Recipe suggestions are currently placeholder implementations
- Search functionality is not yet implemented
- Notification system needs enhancement
- Stats calculations are basic and need improvement

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Bhaswat** ([@THORzero9](https://github.com/THORzero9))

## 🙏 Acknowledgments

- [Appwrite](https://appwrite.io/) - Backend-as-a-Service platform
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit
- [Material Design 3](https://m3.material.io/) - Design system
- [Koin](https://insert-koin.io/) - Dependency injection framework

## 📞 Support

If you encounter any issues or have questions:

1. Check the [Issues](https://github.com/THORzero9/Fresh_Save/issues) section
2. Create a new issue if your problem isn't already reported
3. Provide detailed information about the issue and steps to reproduce

---

**Fresh Save** - Reducing food waste, one item at a time! 🌱
