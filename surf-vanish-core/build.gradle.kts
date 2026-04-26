plugins {
    id("dev.slne.surf.api.gradle.paper-raw")
}

surfRawPaperApi {
    withSurfRedis()
}

dependencies {
    api(project(":surf-vanish-api"))
}