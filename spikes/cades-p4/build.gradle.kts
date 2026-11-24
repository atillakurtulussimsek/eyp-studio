plugins {
    application
}

repositories { mavenCentral() }

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

val dssVersion = "6.5.1"
dependencies {
    implementation("eu.europa.ec.joinup.sd-dss:dss-cades:$dssVersion")
    implementation("eu.europa.ec.joinup.sd-dss:dss-token:$dssVersion")
    implementation("eu.europa.ec.joinup.sd-dss:dss-utils-apache-commons:$dssVersion")
    implementation("eu.europa.ec.joinup.sd-dss:dss-crl-parser-stream:$dssVersion")
    implementation("eu.europa.ec.joinup.sd-dss:dss-cms-object:$dssVersion")
    implementation("eu.europa.ec.joinup.sd-dss:dss-validation:$dssVersion")
    implementation("eu.europa.ec.joinup.sd-dss:dss-policy-jaxb:$dssVersion")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

application { mainClass = "spike.CadesP4Spike" }

tasks.register<Copy>("copyLibs") {
    from(configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("libs-rt"))
}
