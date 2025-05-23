plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.10" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false
}


buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.3.15")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
allprojects {
    repositories {
//        mavenCentral()
//        google()
//        jcenter()
    }
}