val libVersion by extra { "0.3.15" }
val mavenUsername by extra { System.getenv("MAVEN_USERNAME") ?: project.findProperty("mavenUsername") }
val mavenPassword by extra { System.getenv("MAVEN_PASSWORD") ?: project.findProperty("mavenPassword") }

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}