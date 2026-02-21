# 🚨 Women Safety App

![Android](https://img.shields.io/badge/Platform-Android-green)
![Language](https://img.shields.io/badge/Language-Java-orange)
![API](https://img.shields.io/badge/Min%20API-21-blue)
![License](https://img.shields.io/badge/License-MIT-red)

> An Android application designed to help women quickly alert trusted contacts and authorities during emergencies with one-tap SOS, real-time location sharing, and quick-access helpline numbers.

---

## 📸 Screenshots

> Add your app screenshots here

| Home Screen | SOS Alert | Emergency Contacts |
|------------|-----------|-------------------|
| ![Home](link) | ![SOS](link) | ![Contacts](link) |

---

## ✨ Core Features

### 🆘 Emergency SOS System
- **One-Tap SOS Button** - Instantly sends emergency alerts
- **Automatic SMS** to all registered emergency contacts with GPS coordinates
- **Google Maps Link** included in SMS for precise location tracking
- **Optional Auto-Call** to emergency number (configurable)
- **Visual & Audio Feedback** with vibration alerts

### 👥 Emergency Contacts Management
- Add/Edit/Delete trusted emergency contacts
- Store contact details: Name, Phone, Relation, Priority
- Set primary contact for auto-call feature
- Contact validation and duplicate prevention

### 🚓 Quick Police Access
- Direct dial to police helpline (100, 112 in India)
- One-tap call to emergency services
- No typing required in emergency situations

### 📍 Real-Time Location Tracker
- Uses FusedLocationProvider for accurate GPS tracking
- Show current location on map
- Open location directly in Google Maps
- Share location with emergency contacts

### 👤 User Profile Management
- Store personal details (Name, Age, Blood Group)
- Medical information and allergies
- Emergency medical notes
- Quick access during emergencies

### 📞 Helpline Numbers Directory
- Verified helpline numbers database
- Women's helpline, police, ambulance, fire brigade
- One-tap calling feature
- Region-wise helpline numbers

### 📊 Crime Statistics (Optional)
- Search area-wise crime data
- Risk level indicators
- Safety recommendations
- Area safety ratings

### ⚙️ Customizable Settings
- Toggle auto-call on SOS
- Vibration settings
- Repeat SMS feature
- SOS countdown timer
- Notification preferences

---

## 🛠️ Technical Architecture

### Built With

| Technology | Purpose |
|-----------|---------|
| **Java** | Primary Programming Language |
| **Android SDK** | Framework |
| **Room Database** | Local data persistence |
| **FusedLocationProvider** | GPS & Location Services |
| **Material Design** | Modern UI Components |
| **SmsManager** | Emergency SMS functionality |
| **Google Maps API** | Location visualization |

### Architecture Pattern
- **MVVM** (Model-View-ViewModel)
- **Repository Pattern** for data abstraction
- **LiveData** for reactive UI updates
- **Single Activity** with Jetpack Navigation

---

## 📋 System Requirements

### Development Environment
- **Android Studio**: Arctic Fox or later
- **JDK**: Version 11 or higher
- **Gradle**: 7.0+

### Android Device Requirements
- **Min SDK**: 21 (Android 5.0 Lollipop)
- **Target SDK**: 34 (Android 14)
- **GPS**: Required
- **Internet**: Optional (for Maps)

---

## 🚀 Getting Started

### Step 1: Clone the Repository

```bash
git clone https://github.com/PRATHAMESHGORAD/women-safety-app.git
cd women-safety-app
```

### Step 2: Open in Android Studio

1. Launch **Android Studio**
2. Select **File → Open**
3. Navigate to the cloned project folder
4. Wait for Gradle sync to complete

### Step 3: Configure Google Maps API (Optional)

1. Get your API key from [Google Cloud Console](https://console.cloud.google.com/)
2. Open `local.properties`
3. Add:
```properties
MAPS_API_KEY=your_google_maps_api_key_here
```

### Step 4: Grant Required Permissions

The app will request these permissions at runtime:
- 📍 Location (Fine & Coarse)
- 📱 Send SMS
- ☎️ Make Phone Calls
- 📳 Vibration

### Step 5: Build and Run

1. Connect your Android device or start an emulator
2. Click **Run** button (▶️) or press `Shift + F10`
3. Select your device
4. App will install and launch

---

## 📁 Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/womensafety/
│   │   │   ├── activities/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── SOSActivity.java
│   │   │   │   ├── ContactsActivity.java
│   │   │   │   ├── ProfileActivity.java
│   │   │   │   └── LocationActivity.java
│   │   │   ├── database/
│   │   │   │   ├── AppDatabase.java
│   │   │   │   ├── ContactDao.java
│   │   │   │   └── UserDao.java
│   │   │   ├── models/
│   │   │   │   ├── EmergencyContact.java
│   │   │   │   ├── UserProfile.java
│   │   │   │   └── Helpline.java
│   │   │   ├── adapters/
│   │   │   │   ├── ContactsAdapter.java
│   │   │   │   └── HelplineAdapter.java
│   │   │   ├── utils/
│   │   │   │   ├── LocationHelper.java
│   │   │   │   ├── SMSHelper.java
│   │   │   │   └── PermissionHelper.java
│   │   │   └── viewmodels/
│   │   │       ├── ContactViewModel.java
│   │   │       └── ProfileViewModel.java
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   ├── drawable/
│   │   │   ├── values/
│   │   │   └── navigation/
│   │   └── AndroidManifest.xml
│   └── build.gradle
└── build.gradle
```

---

## 🔧 Key Implementation Details

### 1. Emergency SOS Flow

```java
// Get current location
FusedLocationProviderClient fusedLocationClient;
fusedLocationClient.getCurrentLocation(...)
    .addOnSuccessListener(location -> {
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        String mapsLink = "https://maps.google.com/?q=" + lat + "," + lng;
        
        // Send SMS to all contacts
        sendEmergencySMS(mapsLink);
    });
```

### 2. SMS Sending

```java
SmsManager smsManager = SmsManager.getDefault();
String message = "SOS! I need help. My location: " + mapsLink + 
                 ". Name: " + userName;
                 
for (EmergencyContact contact : contacts) {
    smsManager.sendTextMessage(
        contact.getPhone(), 
        null, 
        message, 
        null, 
        null
    );
}
```

### 3. Room Database Schema

```java
@Entity(tableName = "emergency_contacts")
public class EmergencyContact {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String phone;
    private String relation;
    private int priority;
    private boolean isPrimary;
}
```

---

## 📦 Dependencies

```gradle
dependencies {
    // Core Android
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // Room Database
    implementation "androidx.room:room-runtime:2.5.2"
    annotationProcessor "androidx.room:room-compiler:2.5.2"
    
    // Location Services
    implementation 'com.google.android.gms:play-services-location:21.1.0'
    implementation 'com.google.android.gms:play-services-maps:18.2.0'
    
    // Navigation
    implementation 'androidx.navigation:navigation-fragment:2.7.6'
    implementation 'androidx.navigation:navigation-ui:2.7.6'
    
    // Lifecycle Components
    implementation 'androidx.lifecycle:lifecycle-viewmodel:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata:2.7.0'
}
```

---

## 🔐 Required Permissions

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.SEND_SMS"/>
<uses-permission android:name="android.permission.CALL_PHONE"/>
<uses-permission android:name="android.permission.VIBRATE"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```

---

## 📱 How to Use

### First Time Setup
1. **Register Emergency Contacts** - Add at least 3 trusted contacts
2. **Set Primary Contact** - Choose one contact for auto-call
3. **Fill Profile** - Add your details and medical information
4. **Grant Permissions** - Allow location, SMS, and phone permissions

### In Emergency
1. **Tap SOS Button** - Long press for 3 seconds
2. **Automatic Alert** - SMS sent to all contacts with your location
3. **Auto-Call** (if enabled) - Calls primary contact automatically
4. **Wait for Help** - Your location is continuously shared

---

## 🧪 Testing Checklist

- [x] SOS button sends SMS to all registered contacts
- [x] Location link opens correctly in Google Maps
- [x] Auto-call feature works with primary contact
- [x] Add/Edit/Delete contacts functionality
- [x] Permission handling (grant/deny scenarios)
- [x] Location unavailable fallback
- [x] SMS sending failure handling
- [x] Profile data persistence
- [x] App works in Doze mode
- [x] Background location tracking

---

## 🐛 Known Issues & Limitations

- SMS may fail on Android 13+ if app is not set as default SMS app
- Location accuracy depends on GPS signal strength
- Background location tracking limited by battery optimization
- Crime stats feature requires API integration (coming soon)

---

## 🗺️ Roadmap

- [ ] Cloud backup for contacts and profile
- [ ] Live police API integration
- [ ] Audio/Video recording during emergency
- [ ] AI-powered threat detection
- [ ] Multi-language support
- [ ] Integration with police control room
- [ ] Community safety network
- [ ] Panic button widget for lock screen

---

## 🤝 Contributing

Contributions make this project better! Here's how you can help:

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Prathamesh Gorad**
- GitHub: [@PRATHAMESHGORAD](https://github.com/PRATHAMESHGORAD)
- Email: prathameshgorad@example.com

---

## 🙏 Acknowledgements

- [Android Developers](https://developer.android.com/)
- [Material Design Guidelines](https://material.io/)
- [Google Maps Platform](https://developers.google.com/maps)
- [FusedLocationProvider](https://developers.google.com/location-context/fused-location-provider)
- Women's safety organizations for valuable feedback

---

## 📞 Important Helpline Numbers (India)

| Service | Number |
|---------|--------|
| Women Helpline | 1091 |
| Police | 100, 112 |
| Ambulance | 102, 108 |
| National Commission for Women | 7827170170 |

---

## ⚠️ Disclaimer

This app is designed to assist in emergency situations but should not be considered a replacement for professional emergency services. Always contact local authorities in case of immediate danger.

---

<div align="center">
  <p>Made with ❤️ for Women's Safety</p>
  <p>⭐ Star this repo if you found it helpful!</p>
  <p>🚨 Download APK: [Releases](https://github.com/PRATHAMESHGORAD/women-safety-app/releases)</p>
</div>