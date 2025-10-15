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

    // === Code generators ===
//    smithyBuild("software.amazon.smithy.java.codegen:plugins:$smithyJavaVersion")

    // Adds the `@restJson1` protocol trait
    api("software.amazon.smithy:smithy-aws-traits:$smithyVersion")

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

// Add generated source code to the compilation sourceSet
//afterEvaluate {
//    val typesPath = smithy.getPluginProjectionPath(smithy.sourceProjection.get(), "java-type-codegen")
//    sourceSets.main.get().java.srcDir(typesPath)
//}

//tasks.named("compileJava") {
//    dependsOn("smithyBuild")
//}
