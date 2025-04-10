plugins {
    id("java")
    id("application")
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
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "ch.ywesee.Main"
}
