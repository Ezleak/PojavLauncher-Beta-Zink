plugins {
    `maven-publish`
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

version = "0.0.1"

publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components["java"])
            }
        }
    }
}

base {
    archivesName = "TouchController-Proxy-Client"
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.core)
}

kotlin {
    jvmToolchain(8)
}
