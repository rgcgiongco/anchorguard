import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun releaseSigningValue(propertyName: String, envName: String): String? =
    (localProperties.getProperty(propertyName) ?: System.getenv(envName))
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

/* ---------- Auto-incrementing version ---------- */
val versionFile = rootProject.file("version.properties")
val versionProps = Properties().apply {
    if (versionFile.exists()) {
        versionFile.inputStream().use { load(it) }
    }
}
val currentVersionName = versionProps.getProperty("versionName", "0.1.0")
val currentVersionCode = versionProps.getProperty("versionCode", "1").toInt()

val versionParts = currentVersionName.split(".")
val versionMajor = versionParts[0].toInt()
val versionMinor = versionParts[1].toInt()
val versionPatch = versionParts[2].toInt()

android {
    namespace = "com.anchorguard.client"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.anchorguard.client"
        minSdk = 29
        targetSdk = 36
        versionCode = currentVersionCode
        versionName = currentVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePath = releaseSigningValue("ANCHORGUARD_KEYSTORE_FILE", "ANCHORGUARD_KEYSTORE_FILE")
            val keystorePassword = releaseSigningValue("ANCHORGUARD_KEYSTORE_PASSWORD", "ANCHORGUARD_KEYSTORE_PASSWORD")
            val keyAliasValue = releaseSigningValue("ANCHORGUARD_KEY_ALIAS", "ANCHORGUARD_KEY_ALIAS")
            val keyPasswordValue = releaseSigningValue("ANCHORGUARD_KEY_PASSWORD", "ANCHORGUARD_KEY_PASSWORD")

            if (keystorePath != null && keystorePassword != null && keyAliasValue != null && keyPasswordValue != null) {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                keyAlias = keyAliasValue
                keyPassword = keyPasswordValue
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false   // change from true to false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            val releaseSigning = signingConfigs.getByName("release")
            if (releaseSigning.storeFile != null) {
                signingConfig = releaseSigning
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.biometric)

    implementation(libs.google.play.services.location)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

/* ---------- Version increment task ---------- */
tasks.register("incrementVersion") {
    group = "versioning"
    description = "Increments the patch version (e.g. 0.1.0 -> 0.1.1) and versionCode"
    doLast {
        val newPatch = versionPatch + 1
        val newVersionName = "$versionMajor.$versionMinor.$newPatch"
        val newVersionCode = currentVersionCode + 1
        versionProps.setProperty("versionName", newVersionName)
        versionProps.setProperty("versionCode", newVersionCode.toString())
        versionFile.outputStream().use { versionProps.store(it, "Auto-incremented by AnchorGuard build") }
        println("Version incremented: $currentVersionName -> $newVersionName (code: $currentVersionCode -> $newVersionCode)")
    }
}

afterEvaluate {
    tasks.findByName("assembleDebug")?.dependsOn("incrementVersion")
    tasks.findByName("assembleRelease")?.dependsOn("incrementVersion")
}

/* ---------- Rename APK with version ---------- */
tasks.register("renameApk") {
    group = "versioning"
    description = "Renames output APKs to include the version number"
    dependsOn("assembleDebug", "assembleRelease")
    doLast {
        val props = Properties()
        val vf = rootProject.file("version.properties")
        if (vf.exists()) {
            vf.inputStream().use { props.load(it) }
        }
        val versionName = props.getProperty("versionName", "0.1.0")

        val apkDir = File(buildDir, "outputs/apk")
        if (apkDir.exists()) {
            apkDir.walkTopDown().filter { it.extension == "apk" }.forEach { apkFile ->
                val newName = "anchorguard_$versionName.apk"
                val newFile = File(apkFile.parentFile, newName)
                if (apkFile.renameTo(newFile)) {
                    println("APK renamed: ${apkFile.name} -> $newName")
                }
            }
        }
    }
}

/* To auto-rename after build, run: ./gradlew assembleDebug renameApk
   Or add finalizedBy wiring in afterEvaluate block */
