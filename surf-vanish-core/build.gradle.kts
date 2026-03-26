plugins {
    id("dev.slne.surf.surfapi.gradle.paper-raw")
}

dependencies {
    api(project(":surf-vanish-api"))
    api(project(":surf-vanish-api-redis"))
}