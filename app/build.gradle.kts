plugins {
    id("com.android.application") version "8.6.1"
    id("org.jetbrains.kotlin.android") version "2.0.21"
}
android {
    namespace = "org.aysin.typing"
    compileSdk = 35
    defaultConfig {
        applicationId = "org.aysin.typing"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0-dev"
    }
}
dependencies {
    testImplementation("junit:junit:4.13.2")
}
