plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android { namespace = "com.memio.notes"; compileSdk = 35
 defaultConfig { applicationId = "com.memio.notes"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "1.0" } }
kotlin { jvmToolchain(17) }