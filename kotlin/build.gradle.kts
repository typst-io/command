plugins {
    `java-library`
    kotlin("jvm")
    id("convention-publish")
}

dependencies {
    api(project(":command-core"))
    implementation(kotlin("stdlib-jdk8", libs.versions.kotlin.get()))
}
