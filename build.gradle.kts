import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

plugins {
    id("java")
    id("io.github.goooler.shadow") version "8.1.7"
}

// Library versions
val spigot = property("spigot") as String
val bstats = property("bstats") as String
val junit = property("junit") as String
val lombok = property("lombok") as String
val hikaricp = property("hikaricp") as String
val jooq = property("jooq") as String
val projectVersion: String by project
group = "kz.hxncus.mc"
version = property("projectVersion") as String

allprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
        maven("https://papermc.io/repo/repository/maven-public/") // Paper
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://libraries.minecraft.net/") // Minecraft repo
        maven("https://maven.enginehub.org/repo/")
        maven("https://jitpack.io") // JitPack
        mavenLocal()
    }

    dependencies {
        compileOnly("org.spigotmc:spigot-api:$spigot")
        compileOnly(fileTree("../libs/compileOnly/"))
        compileOnly("org.projectlombok:lombok:$lombok")

        implementation("org.bstats:bstats-bukkit:$bstats")
        implementation("com.zaxxer:HikariCP:$hikaricp")
        implementation("org.jooq:jooq:$jooq")
        implementation(fileTree("../libs/implementation/"))

        annotationProcessor("org.projectlombok:lombok:$lombok")
        testAnnotationProcessor("org.projectlombok:lombok:$lombok")
        testCompileOnly("org.projectlombok:lombok:$lombok")
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junit")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {

}

tasks {
    compileJava.get().options.encoding = Charsets.UTF_8.name()
    javadoc.get().options.encoding = Charsets.UTF_8.name()
    processResources {
        filesNotMatching(listOf("**/*.png", "**/*.ogg", "**/models/**", "**/textures/**", "**/font/**.json", "**/plugin.yml")) {
            expand(mapOf(project.version.toString() to projectVersion))
        }
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filteringCharset = Charsets.UTF_8.name()
    }
    shadowJar {
        archiveClassifier.set("")
        relocate("org.bstats", "kz.hxncus.mc.simplepenalty.metrics")
        relocate("com.zaxxer", "kz.hxncus.mc.simplepenalty.libs.hikaricp")
        relocate("org.jooq", "kz.hxncus.mc.simplepenalty.libs.jooq")
        manifest {
            attributes(
                mapOf(
                    "Built-By" to System.getProperty("user.name"),
                    "Version" to projectVersion,
                    "Build-Timestamp" to SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss.SSSZ").format(Date.from(Instant.now())),
                    "Created-By" to "Gradle ${gradle.gradleVersion}",
                    "Build-Jdk" to "${System.getProperty("java.version")} ${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")}",
                    "Build-OS" to "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}",
                    "Compiled" to (project.findProperty("compiled")?.toString() ?: "true").toBoolean()
                )
            )
        }
        archiveFileName.set("SimplePenalty-${projectVersion}.jar")
        archiveClassifier.set("")
    }
    compileJava.get().dependsOn(clean)
    build.get().dependsOn(shadowJar)
}
