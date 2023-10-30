import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    `java-library`
    application

    id("com.github.ben-manes.versions") version ("0.49.0")
    id("edu.sc.seis.launch4j") version ("3.0.5")

    kotlin("jvm") version ("1.9.20")
}

group = "dev.blocky.app.vx"
version = "1.3.0"
description = "General utility program written in Java."

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.openjfx:javafx-base:21.0.1:win")
    implementation("org.openjfx:javafx-graphics:21.0.1:win")
    implementation("org.openjfx:javafx-controls:21.0.1:win")
    implementation("org.openjfx:javafx-media:21.0.1:win")
    implementation("org.openjfx:javafx-web:21.0.1:win")
    implementation("org.openjfx:javafx-swing:21.0.1:win")

    implementation("net.java.dev.jna:jna-platform:5.13.0")

    api("org.kohsuke:github-api:1.317")

    api("net.lingala.zip4j:zip4j:2.11.5")

    api("com.documents4j:documents4j-api:1.1.12")
    api("com.documents4j:documents4j-local:1.1.12")
    api("com.documents4j:documents4j-transformer-msoffice-word:1.1.12")
    api("com.documents4j:documents4j-transformer-msoffice-powerpoint:1.1.12")

    api("com.google.zxing:core:3.5.2")
    api("com.google.zxing:javase:3.5.2")

    api("org.controlsfx:controlsfx:11.1.2")

    api("org.apache.commons:commons-lang3:3.13.0")
    api("commons-io:commons-io:2.15.0")
    api("commons-codec:commons-codec:1.16.0")

    api("org.slf4j:slf4j-api:2.0.9")
    api("ch.qos.logback:logback-classic:1.4.11")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainModule.set("dev.blocky.app.vx")
    mainClass.set("dev.blocky.app.vx.Main")
}

launch4j {
    outfile = "vxar.exe"
    mainClassName = "dev.blocky.app.vx.Main"

    outputDir = "D:\\VorteX-Builds"
    libraryDir = "D:\\VorteX-Builds\\lib"

    icon = "${projectDir}/src/main/resources/assets/icons/icon.ico"

    bundledJrePath = System.getenv("JAVA_HOME")
    jreMinVersion = "17"

    version = project.version.toString()
    textVersion = project.version.toString()
    copyright = "Copyright 2023 Dominic R. (aka. BlockyDotJar)"
    downloadUrl = "https://www.oracle.com/java/technologies/downloads/#jdk17-windows"
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Module"] = "dev.blocky.app.vx"
        attributes["Main-Class"] = "dev.blocky.app.vx.Main"
        attributes["Automatic-Module-Name"] = "VorteX"
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.compileClasspath.map { config -> config.map { if (it.isDirectory) it else zipTree(it) } })
}

tasks.withType<JavaCompile> {
    doFirst {
        options.compilerArgs.addAll(arrayOf("--module-path", classpath.asPath, "--add-modules", "javafx.base,javafx.controls,javafx.graphics,javafx.media,javafx.web,javafx.swing"))
    }
}

tasks.withType<DependencyUpdatesTask> {
    gradleReleaseChannel = "current"

    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}
