import dev.slne.surf.surfapi.gradle.util.registerRequired

plugins {
    id("dev.slne.surf.surfapi.gradle.paper-plugin")
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
    implementation("dev.slne.surf.tab:surf-tab-api:1.21.11-1.0.2-SNAPSHOT")
}