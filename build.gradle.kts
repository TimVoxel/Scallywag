plugins {
    id("java")
}

group = "me.timpixel"
version = "1.0-SNAPSHOT"
project.version = version

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}
dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
}

tasks.withType<Jar> {
    destinationDirectory.set(File("${projectDir.parentFile}/Test Server/plugins"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
