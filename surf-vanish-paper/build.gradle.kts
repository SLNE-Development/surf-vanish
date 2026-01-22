import dev.slne.surf.surfapi.gradle.util.registerSoft

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.vanish.paper.PaperMain")
    generateLibraryLoader(false)
    foliaSupported(true)

    authors.add("red")

    withSurfRedis()

    serverDependencies {
        registerSoft("MiniPlaceholders")
    }
}

dependencies {
    api(project(":surf-vanish-core"))
    compileOnly(libs.miniplaceholder.api)
    implementation("dev.slne.surf.tab:surf-tab-api:1.21.11-1.0.2-SNAPSHOT")
}