plugins {
    id("java")
}

group = "me.timpixel"
version = "1.0-SNAPSHOT"
project.version = version
project.version = "1.0-SNAPSHOT"

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    mavenCentral()
}
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    implementation("org.mindrot:jbcrypt:0.4")
}

tasks.withType<Jar> {
    destinationDirectory.set(File("${projectDir.parentFile}/Test Server/plugins"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
