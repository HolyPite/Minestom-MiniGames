plugins {
    id("java")
    id("application")
}

group = "me.holypite"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("me.holypite.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.minestom:minestom:2025.10.18-1.21.10")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}