plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "me.timpixel"
version = "1.2.0-SNAPSHOT"
project.version = version

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}
dependencies {
    implementation("org.mindrot:jbcrypt:0.4")
    compileOnly("org.apache.logging.log4j:log4j-core:3.0.0-beta3")
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

tasks.shadowJar {
    archiveClassifier.set("")
    //relocate("org.mindrot.jbcrypt", "me.timpixel.libs.jbcrypt")
}

tasks.withType<Jar> {
    destinationDirectory.set(File("${projectDir.parentFile}/Test Server/plugins"))
}

tasks.register<Copy>("copyJar") {
    from(tasks.shadowJar)
    into("C://Development/Paper/APITester/libs")
}

tasks.shadowJar {
    finalizedBy("copyJar")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
