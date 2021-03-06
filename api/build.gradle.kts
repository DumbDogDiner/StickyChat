plugins {
    kotlin("jvm")
    id("java")
}

repositories {
    mavenCentral()
    jcenter()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.16.3-R0.1-SNAPSHOT")
    implementation("org.jetbrains:annotations:16.0.2")

    testImplementation(kotlin("stdlib"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}


tasks {
        test {
        dependsOn("compileTestKotlin")
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
