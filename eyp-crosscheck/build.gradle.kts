// Test-only harness: compiles the official e-Yazışma Java API 2.1 (docs/references, open source per guide §5.4)
// and opens our packages with it. Nothing from this module is shipped.

sourceSets {
    create("officialApi") {
        java.srcDir(rootProject.file("docs/references/api-java-2.1/extracted/Java/Source/Cbddo/src"))
    }
}

val officialApiImplementation by configurations.getting
configurations.named("testImplementation") { extendsFrom(officialApiImplementation) }

dependencies {
    // Versions named in the official API BENIOKU (Java 2.0+) plus JAXB, which Java 11+ no longer bundles.
    officialApiImplementation("org.apache.poi:poi:4.1.1")
    officialApiImplementation("org.apache.poi:poi-ooxml:4.1.1")
    officialApiImplementation("dom4j:dom4j:1.6.1")
    officialApiImplementation("org.apache.commons:commons-compress:1.19")
    officialApiImplementation("javax.xml.bind:jaxb-api:2.3.1")
    officialApiImplementation("org.glassfish.jaxb:jaxb-runtime:2.3.9")

    testImplementation(sourceSets["officialApi"].output)
    testImplementation(project(":eyp-core"))
    testImplementation(testFixtures(project(":eyp-core")))
}

tasks.named<JavaCompile>("compileOfficialApiJava") {
    options.compilerArgs.removeAll { it.startsWith("-Xlint") }
    options.compilerArgs.add("-nowarn")
    options.isWarnings = false
}
