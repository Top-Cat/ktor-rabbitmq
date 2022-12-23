import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.21"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("maven-publish")
}

val ktorVersion: String by project
val rabbitmqVersion: String by project
val jacksonVersion: String by project
val mockkVersion: String by project
val junitVersion: String by project
val testcontainersVersion: String by project

group = "pl.jutupe"
version = System.getenv("BUILD_NUMBER")?.let { "0.4.$it" } ?: "0.4.0"

publishing {
    val sourceJarTask = task<Jar>("sourceJar") {
        from(java.sourceSets["main"].allSource)
        archiveClassifier.set("sources")
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(sourceJarTask)
        }
    }
    repositories {
        maven {
            name = "reposilite"
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
            url = uri("https://artifactory.kirkstall.top-cat.me/")
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.21")

    // ktor
    implementation("io.ktor:ktor-server-core:$ktorVersion")

    // rabbitmq
    implementation("com.rabbitmq:amqp-client:$rabbitmqVersion")

    // tests
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")

    testImplementation("org.testcontainers:rabbitmq:$testcontainersVersion")
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
}

kotlin {
    target {
        compilations.all {
            kotlinOptions.jvmTarget = "16"
        }
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events = mutableSetOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
    }
}

ktlint {
    reporters {
        reporter(ReporterType.CHECKSTYLE)
    }
}
