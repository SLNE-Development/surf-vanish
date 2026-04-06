import dev.slne.surf.api.gradle.util.registerRequired

plugins {
    id("dev.slne.surf.api.gradle.paper-plugin")
}

surfPaperPluginApi {
    mainClass("dev.slne.surf.vanish.paper.PaperMain")
    generateLibraryLoader(false)
    foliaSupported(true)

    authors.add("red")

    withSurfRedis()
    withCorePaper()

    serverDependencies {
        registerRequired("LuckPerms")
    }
}

dependencies {
    api(project(":surf-vanish-core"))
    compileOnly("net.luckperms:api:5.4")
}