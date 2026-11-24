plugins {
    java
}

allprojects {
    group = "digital.varkhe.eyp"
    version = "0.1.0-SNAPSHOT"
    repositories { mavenCentral() }
}

subprojects {
    apply(plugin = "java")

    java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-serial"))
    }

    dependencies {
        "testImplementation"(platform("org.junit:junit-bom:5.13.4"))
        "testImplementation"("org.junit.jupiter:junit-jupiter")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform { excludeTags("hardware") }
    }

    // Same tests again under Turkish locale (I/ı case-mapping pitfalls).
    val testTr = tasks.register<Test>("testTr") {
        description = "Runs tests with tr_TR default locale."
        group = "verification"
        val test = tasks.named<Test>("test").get()
        testClassesDirs = test.testClassesDirs
        classpath = test.classpath
        jvmArgs("-Duser.language=tr", "-Duser.country=TR")
    }
    tasks.named("check") { dependsOn(testTr) }
}
