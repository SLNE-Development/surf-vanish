import dev.slne.surf.surfapi.gradle.util.registerSoft

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.vanish.paper.PaperMain")
    generateLibraryLoader(false)

    authors.add("red")

    serverDependencies {
        registerSoft("MiniPlaceholders")
    }
}

dependencies {
    api(project(":surf-vanish-core"))
    compileOnly(libs.miniplaceholder.api)
}