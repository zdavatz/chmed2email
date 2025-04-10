plugins {
    id("java")
    id("application")
    id("com.gradleup.shadow") version "9.0.0-beta12"
}

group = "ch.ywesee"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("jakarta.json:jakarta.json-api:2.0.1")
    implementation("org.glassfish:jakarta.json:2.0.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.18.3")
    implementation("commons-cli:commons-cli:1.9.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.google.zxing:javase:3.5.3")
    implementation("org.apache.pdfbox:pdfbox:3.0.4")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "ch.ywesee.Main"
}
