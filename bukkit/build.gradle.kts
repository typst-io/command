plugins {
    `java-library`
    alias(spigots.plugins.spigotBase)
    `maven-publish`
    signing
    id("convention-publish")
    kotlin("jvm")
}

repositories {
    mavenCentral()
    spigotRepos {
        spigotmc()
    }
}

configurations {
    testImplementation {
        extendsFrom(runtimeClasspath.get(), compileClasspath.get())
    }
}

dependencies {
    api(project(":command-core"))
    compileOnly("org.jetbrains:annotations:26.0.2-1")
    compileOnly(spigots.spigot.api)
    compileOnly(commons.lombok)
    annotationProcessor(commons.lombok)
    compileOnly(kotlin("stdlib-jdk8", libs.versions.kotlin.get()))
    testImplementation(enforcedPlatform(commons.junit.bom))
    testImplementation(commons.junit.jupiter)
    testImplementation(commons.assertj.core)
    testImplementation(commons.mockito.core)
    testRuntimeOnly(commons.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    })
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

spigot {
    name = "BukkitCommand"
    commands {
        create("bukkitcommand") {
            aliases = listOf("command", "cmd")
        }
    }
}
