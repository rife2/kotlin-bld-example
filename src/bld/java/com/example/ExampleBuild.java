package com.example;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.extension.CompileKotlinOperation;
import rife.bld.extension.DetektOperation;
import rife.bld.extension.DokkaOperation;
import rife.bld.extension.dokka.LoggingLevel;
import rife.bld.extension.dokka.OutputFormat;
import rife.bld.extension.kotlin.CompileOptions;
import rife.bld.operations.exceptions.ExitStatusException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.compile;
import static rife.bld.dependencies.Scope.test;

public class ExampleBuild extends Project {
    public ExampleBuild() {
        pkg = "com.example";
        name = "Example";
        mainClass = "com.example.Example";
        version = version(0, 1, 0);

        javaRelease = 17;
        downloadSources = true;
        autoDownloadPurge = true;
        repositories = List.of(MAVEN_LOCAL, MAVEN_CENTRAL, RIFE2_RELEASES);

        final var kotlin = version(2, 1, 20);
        scope(compile)
                .include(dependency("org.jetbrains.kotlin", "kotlin-stdlib", kotlin));
        scope(test)
                .include(dependency("org.jetbrains.kotlin", "kotlin-test-junit5", kotlin))
                .include(dependency("org.junit.jupiter", "junit-jupiter", version(5, 12, 1)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1, 12, 1)))
                .include(dependency("org.junit.platform", "junit-platform-launcher", version(1, 12, 1)));

        // Include the Kotlin source directory when creating or publishing sources Java Archives
        jarSourcesOperation().sourceDirectories(new File(srcMainDirectory(), "kotlin"));
    }

    public static void main(String[] args) {
        // Enable detailed logging for the Kotlin extension
//        var level = Level.ALL;
//        var logger = Logger.getLogger("rife.bld.extension");
//        var consoleHandler = new ConsoleHandler();
//
//        consoleHandler.setLevel(level);
//        logger.addHandler(consoleHandler);
//        logger.setLevel(level);
//        logger.setUseParentHandlers(false);

        new ExampleBuild().start(args);
    }

    @BuildCommand(summary = "Compiles the Kotlin project")
    @Override
    public void compile() throws Exception {
        // Avoid warnings in JDK 24
        final var options = new CompileOptions().jvmOptions("--enable-native-access=ALL-UNNAMED");
        // The source code located in src/main/kotlin and src/test/kotlin will be compiled
        new CompileKotlinOperation()
                .fromProject(this)
                .compileOptions(options)
//                .kotlinHome("path/to/kotlin")
//                .kotlinc("path/to/kotlinc")
                .execute();
    }

    @BuildCommand(summary = "Checks source with Detekt")
    public void detekt() throws ExitStatusException, IOException, InterruptedException {
        // The source code located in the project will be checked
        new DetektOperation()
                .fromProject(this)
                .execute();
    }

    @BuildCommand(value = "detekt-baseline", summary = "Creates the Detekt baseline")
    public void detektBaseline() throws ExitStatusException, IOException, InterruptedException {
        // The detekt-baseline.xml file will be created in the project's root
        new DetektOperation()
                .fromProject(this)
                .baseline("detekt-baseline.xml")
                .createBaseline(true)
                .execute();
    }

    @BuildCommand(value = "detekt-main", summary = "Checks main source with Detekt")
    public void detektMain() throws ExitStatusException, IOException, InterruptedException {
        // The source code located in src/main/kotlin will be checked
        new DetektOperation()
                .fromProject(this)
                .input("src/main/kotlin")
                .execute();
    }

    @BuildCommand(value = "detekt-test", summary = "Checks test source with Detekt")
    public void detektTest() throws ExitStatusException, IOException, InterruptedException {
        // The source code located in src/test/kotlin will be checked
        new DetektOperation()
                .fromProject(this)
                .input("src/test/kotlin")
                .execute();
    }

    @BuildCommand(value = "docs", summary = "Generates all documentation")
    public void docs() throws ExitStatusException, IOException, InterruptedException {
        dokkaGfm();
        dokkaHtml();
        dokkaJekyll();
        javadoc();
    }

    @BuildCommand(value = "dokka-gfm", summary = "Generates documentation in GitHub flavored markdown format")
    public void dokkaGfm() throws ExitStatusException, IOException, InterruptedException {
        new DokkaOperation()
                .fromProject(this)
                .loggingLevel(LoggingLevel.INFO)
                // Create build/dokka/gfm
                .outputDir(Path.of(buildDirectory().getAbsolutePath(), "dokka", "gfm").toFile())
                .outputFormat(OutputFormat.MARKDOWN)
                .execute();
    }

    @BuildCommand(value = "dokka-html", summary = "Generates documentation in HTML format")
    public void dokkaHtml() throws ExitStatusException, IOException, InterruptedException {
        new DokkaOperation()
                .fromProject(this)
                .loggingLevel(LoggingLevel.INFO)
                // Create build/dokka/html
                .outputDir(Path.of(buildDirectory().getAbsolutePath(), "dokka", "html").toFile())
                .outputFormat(OutputFormat.HTML)
                .execute();
    }

    @BuildCommand(value = "dokka-jekyll", summary = "Generates documentation in Jekyll flavored markdown format")
    public void dokkaJekyll() throws ExitStatusException, IOException, InterruptedException {
        new DokkaOperation()
                .fromProject(this)
                .loggingLevel(LoggingLevel.INFO)
                // Create build/dokka/jekyll
                .outputDir(Path.of(buildDirectory().getAbsolutePath(), "dokka", "jekkyl").toFile())
                .outputFormat(OutputFormat.JEKYLL)
                .execute();
    }

    @BuildCommand(summary = "Generates Javadoc for the project")
    @Override
    public void javadoc() throws ExitStatusException, IOException, InterruptedException {
        new DokkaOperation()
                .fromProject(this)
                .loggingLevel(LoggingLevel.INFO)
                // Create build/javadoc
                .outputDir(new File(buildDirectory(), "javadoc"))
                .outputFormat(OutputFormat.JAVADOC)
                .execute();
    }
}
