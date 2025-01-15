
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.minipost"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.minipost"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation("androidx.navigation:navigation-fragment:2.7.4")
    //noinspection UseTomlInstead
    implementation("androidx.navigation:navigation-ui:2.7.4")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.fragment:fragment-ktx:1.6.1")

    implementation ("commons-io:commons-io:2.11.0")
    // Retrofit 依赖
    implementation(libs.retrofit)

    // Gson 转换器
    implementation(libs.converter.gson)

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.extension.okhttp)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)




}