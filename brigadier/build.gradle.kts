plugins {
    `java-library`
    alias(bungees.plugins.bungeeBase)
    id("convention-publish")
    kotlin("jvm")
}

repositories {
    mavenCentral()
    bungeeRepos {
        minecraftLibraries()
    }
}

configurations {
    testImplementation {
        extendsFrom(runtimeClasspath.get(), compileClasspath.get())
    }
}

dependencies {
    api(project(":command-core"))
    implementation(kotlin("stdlib-jdk8", libs.versions.kotlin.get()))
    compileOnly(bungees.brigadier)
    testImplementation(enforcedPlatform(commons.junit.bom))
    testImplementation(commons.junit.jupiter)
    testImplementation(commons.mockito.core)
    testImplementation(commons.assertj.core)
    testRuntimeOnly(commons.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
}