import android.databinding.tool.ext.classSpec

val libVersion: String by rootProject.extra
val mavenUsername: String by rootProject.extra
val mavenPassword: String by rootProject.extra

plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}


android {
    namespace = "ro.clockworks.clocclib.core"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        aarMetadata {
            minCompileSdk = 24
        }
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

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {

    implementation(libs.guice)
    implementation(libs.hardware)
    implementation(libs.ftccommon)
    implementation(libs.robotcore)
    implementation(libs.jacksoncore)
    implementation(libs.jacksondatabind)
    implementation(libs.jacksonannotations)


    testImplementation(libs.junit)
}

publishing {
    repositories {
        maven {
            url = uri("https://mvn.lucaci32u4.xyz/releases")
            credentials {
                username = mavenUsername
                password = mavenPassword
            }

        }
    }
    publications {
        register<MavenPublication>("release") {
            version = libVersion
            groupId = "ro.clockworks.clocclib"
            artifactId = project.name

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}