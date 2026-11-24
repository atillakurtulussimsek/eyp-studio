plugins {
    application
}

description = "Command line interface: olustur, incele, dogrula."

dependencies {
    implementation(project(":eyp-core"))
    implementation("info.picocli:picocli:4.7.7")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.22.4")
    testImplementation(testFixtures(project(":eyp-core")))
}

application {
    mainClass = "digital.varkhe.eyp.cli.EypCli"
    applicationName = "eyp"
}
