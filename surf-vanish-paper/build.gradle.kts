plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.vanish.paper.PaperMain")
    generateLibraryLoader(false)

    authors.add("red")
}

dependencies {
    api(project(":surf-vanish-core"))
}