plugins {
    id("java")
    id("maven-publish")
    id("com.gradleup.shadow") version ("9.0.0-beta12")
}

group = "com.artillexstudios.axcoins"
version = "1.0.0"

dependencies {
    implementation(project(":api"))
    implementation(project(":common"))
}

allprojects {
    repositories {
        mavenCentral()

        maven("https://jitpack.io/")
        maven("https://repo.artillex-studios.com/releases/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

subprojects {
    apply {
        plugin("java")
        plugin("maven-publish")
        plugin("com.gradleup.shadow")
    }

    dependencies {
        implementation("com.artillexstudios.axapi:axapi:1.4.721:all")
        compileOnly("com.github.ben-manes.caffeine:caffeine:3.2.0")
        compileOnly("org.spigotmc:spigot-api:1.21-R0.1-SNAPSHOT")
        compileOnly("dev.jorel:commandapi-bukkit-shade:10.1.0")
        compileOnly("org.apache.commons:commons-lang3:3.14.0")
        compileOnly("com.github.MilkBowl:VaultAPI:1.7")
        compileOnly("me.clip:placeholderapi:2.11.6")
        compileOnly("commons-io:commons-io:2.16.1")
        compileOnly("it.unimi.dsi:fastutil:8.5.13")
        compileOnly("org.slf4j:slf4j-api:2.0.9")
        compileOnly("com.h2database:h2:2.3.232")
        compileOnly("com.zaxxer:HikariCP:5.1.0")
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        relocate("com.github.benmanes", "com.artillexstudios.axcoins.libs.axapi.libs.caffeine")
        relocate("com.artillexstudios.axapi", "com.artillexstudios.axcoins.libs.axapi")
        relocate("dev.jorel.commandapi", "com.artillexstudios.axcoins.libs.commandapi")
        relocate("com.zaxxer", "com.artillexstudios.axcoins.libs.hikaricp")
        relocate("org.h2", "com.artillexstudios.axcoins.libs.h2")
    }
}