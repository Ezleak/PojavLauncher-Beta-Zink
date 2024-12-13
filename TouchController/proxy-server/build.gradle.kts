plugins {
    `maven-publish`
    alias(libs.plugins.jetbrains.kotlin.jvm)
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
    archivesName = "TouchController-Proxy-Server"
}

dependencies {
    implementation(project(":proxy-client"))
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}
