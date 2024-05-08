plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.adventurecompass"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.adventurecompass"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database:20.3.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.firebaseui:firebase-ui-database:8.0.2")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.orhanobut:dialogplus:1.11@aar")

    // Firebase BOM (Bill of Materials)
    //noinspection BomWithoutPlatform
    implementation ("com.google.firebase:firebase-bom:32.8.1") // Проверете за актуалните версии
    // Firebase Authentication
    implementation ("com.google.firebase:firebase-auth")
}
apply (plugin = "com.google.gms.google-services")
apply(plugin = "com.android.application")