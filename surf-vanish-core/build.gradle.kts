plugins {
    id("dev.slne.surf.surfapi.gradle.core")
}

dependencies {
    api(project(":surf-vanish-api"))
    api(project(":surf-vanish-api-redis"))
}