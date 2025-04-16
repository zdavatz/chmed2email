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
    // https://mvnrepository.com/artifact/com.sun.mail/jakarta.mail
    implementation("com.sun.mail:jakarta.mail:2.0.1")
    // https://mvnrepository.com/artifact/org.apache.httpcomponents.client5/httpclient5
    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.3")
    // https://mvnrepository.com/artifact/org.apache.httpcomponents.client5/httpclient5-fluent
    implementation("org.apache.httpcomponents.client5:httpclient5-fluent:5.4.3")
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-api:2.0.17")
    // https://mvnrepository.com/artifact/org.slf4j/slf4j-simple
    implementation("org.slf4j:slf4j-simple:2.0.17")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "ch.ywesee.Main"
}
