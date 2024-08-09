import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm("desktop") {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.androidx.material3.desktop)
                implementation(libs.androidx.material3.adaptive.navigation.suite.desktop)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.lifecycle.viewmodel.ktx)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.androidx.lifecycle.viewmodel.savedstate)
                implementation(compose.preview)
                implementation(libs.play.services.nearby)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.material3.adaptive.navigation.suite.android)
                implementation(libs.androidx.material.icons.extended)
                implementation(libs.accompanist.permissions)
                implementation(libs.androidx.work.runtime.ktx)

            }
        }
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.components.resources)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.androidx.datastore.preferences.core)
                implementation(libs.androidx.datastore.core)
                implementation(libs.navigation.compose)
                implementation(libs.androidx.ui.graphics)
                implementation(libs.glide.compose)
                implementation(libs.coil.compose)
                implementation(libs.coil)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.android)
            }
        }
    }
}

android {
    namespace = "com.kastik.files"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.kastik.files"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
        debugImplementation(libs.leakcanary.android)
    }
    lint {
        baseline = file("lint-baseline.xml")
    }

}


compose.desktop {
    application {
        mainClass = "Main"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.kastik.files"
            packageVersion = "1.0.0"
        }
    }
}
