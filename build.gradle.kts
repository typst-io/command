import java.net.URL
import java.util.*

plugins {
    base
    `java-base`
    alias(spigots.plugins.spigotBase) apply false
    alias(bungees.plugins.bungeeBase) apply false
    kotlin("jvm") version libs.versions.kotlin apply false
}

val ossrhUsername = providers.gradleProperty("ossrh.username")
val ossrhPassword = providers.gradleProperty("ossrh.password")

project.tasks.register("publishCentralPortal") {
    group = "publishing"
    val projectGroup = project.group
    doLast {
        val url = URL("https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/${projectGroup}")
        println(url)
        val con = url.openConnection() as java.net.HttpURLConnection
        val username = ossrhUsername.get()
        val password = ossrhPassword.get()
        val credential = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
        val authValue = "Bearer $credential"
        con.requestMethod = "POST"
        con.setRequestProperty("Authorization", authValue)
        println(con.responseCode)
        assert(con.responseCode == 200)
    }
}

allprojects {
    pluginManager.apply("java-base")

    group = "io.typst"
    version = "3.2.0"

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }
}
