plugins {
    scala
    `java-library`
    `maven-publish`
    signing
    id("convention-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.scala-lang:scala3-library_3:3.7.1")
    api(project(":command-core"))
}
