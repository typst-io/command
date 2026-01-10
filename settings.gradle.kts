rootProject.name = "command"

include("core", "bukkit", "scala", "kotlin", "brigadier")

rootProject.children.forEach { project ->
    project.name = "command-${project.name}"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    }
    versionCatalogs {
        create("spigots") {
            version("spigot-api", "1.20.1-R0.1-SNAPSHOT")
            version("paperweight", "1.7.7")
            from("io.typst:spigot-catalog:1.0.0")
        }
        create("bungees") {
            from("io.typst:bungee-catalog:1.0.0")
        }
        create("commons") {
            from("io.typst:common-catalog:1.0.1")
            version("junit", "5.14.1")
        }
    }
}

includeBuild("build-logic")
