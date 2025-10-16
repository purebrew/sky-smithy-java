description = "Example protocol extensions."

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

plugins {
    `java-library`
    id("maven-publish")
    // Packages the models in this package into a jar for sharing/distribution by other packages
    id("software.amazon.smithy.gradle.smithy-jar").version("1.1.0")
}

dependencies {
    val smithyVersion: String by project
    val smithyJavaVersion: String by project

    // Adds the `@restJson1` protocol trait
    api("software.amazon.smithy:smithy-aws-traits:$smithyVersion")
    api("software.amazon.smithy:smithy-build:${smithyVersion}")

    implementation("software.amazon.smithy.java:core:${smithyJavaVersion}")
}

// Helps the Smithy IntelliJ plugin identify models
sourceSets {
    main {
        java {
            srcDir("model")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "tech.purebrew.smithy"
            artifactId = "protocol"
            version = "0.1.0-SNAPSHOT"

            from(components["java"])
        }
    }
}
