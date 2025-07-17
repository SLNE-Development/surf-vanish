import com.github.jengelman.gradle.plugins.shadow.ShadowJavaPlugin.Companion.shadowJar

buildscript {
    repositories {
        gradlePluginPortal()
        maven("https://repo.slne.dev/repository/maven-public/") { name = "maven-public" }
    }
    dependencies {
        classpath("dev.slne.surf:surf-api-gradle-plugin:1.21.7+")
    }
}

allprojects {
    group = "dev.slne.surf.vanish"
    version = findProperty("version") ?: "Unknown"
}