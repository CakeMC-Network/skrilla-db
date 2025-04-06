plugins {
    kotlin("jvm") version "2.0.0"
}

group = "net.cakemc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

@Suppress("unchecked_cast")
fun <V> prop(value: String): V {
    return properties.getValue(value) as V
}

dependencies {
    implementation(
        group = "net.java.dev.jna",
        name = "jna",
        version = prop("dep-jna"),
    )
    implementation(
        group = "io.netty",
        name = "netty-all",
        version = prop("dep-netty")
    )
    implementation(
        group = "com.github.luben",
        name = "zstd-jni",
        version = prop("dep-zstd")
    )
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}