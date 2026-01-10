plugins {
    java
    signing
    `maven-publish`
}

val projectName = provider { name }
val moduleName = provider { name.substring(name.indexOf('-') + 1) }
val projectGroup = provider { group.toString() }

publishing {
    publications {
        register(moduleName.get(), MavenPublication::class.java) {
            from(components["java"])
            pom {
                name = provider {
                    "${projectGroup.get()}:${projectName.get()}"
                }
                description.set("Pure, functional command line parser.")
                url.set("https://github.com/typst-io/command")
                licenses {
                    license {
                        name.set("The GNU General Public License, Version 3.0")
                        url.set("https://www.gnu.org/licenses/gpl-3.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("entrypointkr")
                        name.set("Junhyung Im")
                        email.set("entrypointkr@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/typst-io/command.git")
                    developerConnection.set("scm:git:ssh://github.com:typst-io/command.git")
                    url.set("https://github.com/typst-io/command/tree/master")
                }
            }
            // NOTE: https://central.sonatype.org/pages/ossrh-eol/
            repositories {
                maven {
                    name = "sonatypeReleases"
                    url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                    credentials {
                        username = findProperty("ossrhUsername")?.toString()
                        password = findProperty("ossrhPassword")?.toString()
                    }
                }
            }
        }
        signing {
            sign(publishing.publications[moduleName.get()])
        }
        java {
            withSourcesJar()
            withJavadocJar()
        }
        tasks.javadoc {
            options.encoding = "UTF-8"
        }
    }
}