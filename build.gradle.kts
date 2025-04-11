val libVersion by extra { "0.3.1" }
val mavenUsername by extra { project.findProperty("mavenUsername") ?: System.getenv("MAVEN_USERNAME") }
val mavenPassword by extra { project.findProperty("mavenPassword") ?: System.getenv("MAVEN_PASSWORD") }

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}