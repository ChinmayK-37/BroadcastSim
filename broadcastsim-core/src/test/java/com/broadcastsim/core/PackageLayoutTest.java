package com.broadcastsim.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Verifies that Java source directories match their declared package names. */
class PackageLayoutTest {

  private static final Path MAIN_SOURCE_ROOT = Path.of("src", "main", "java");
  private static final Path TEST_SOURCE_ROOT = Path.of("src", "test", "java");
  private static final Pattern PACKAGE_DECLARATION = Pattern.compile("(?m)^package\\s+([\\w.]+);");
  private static final Pattern PATH_SEPARATOR = Pattern.compile("[\\\\/]");

  @Test
  void productionSourceDirectoriesMatchDeclaredPackages() throws IOException {
    assertPackagesMatchDirectories(MAIN_SOURCE_ROOT);
  }

  @Test
  void testSourceDirectoriesMatchDeclaredPackages() throws IOException {
    assertPackagesMatchDirectories(TEST_SOURCE_ROOT);
  }

  private void assertPackagesMatchDirectories(Path sourceRoot) throws IOException {
    try (Stream<Path> sourceFiles = Files.walk(sourceRoot)) {
      sourceFiles
          .filter(path -> path.toString().endsWith(".java"))
          .forEach(path -> assertPackageMatchesDirectory(sourceRoot, path));
    }
  }

  private void assertPackageMatchesDirectory(Path sourceRoot, Path sourceFile) {
    String source = readSource(sourceFile);
    Matcher matcher = PACKAGE_DECLARATION.matcher(source);
    assertEquals(true, matcher.find(), () -> "missing package declaration: " + sourceFile);
    String expectedPackage =
        PATH_SEPARATOR
            .matcher(sourceRoot.relativize(sourceFile.getParent()).toString())
            .replaceAll(".");
    assertEquals(expectedPackage, matcher.group(1), () -> "package mismatch: " + sourceFile);
  }

  private String readSource(Path sourceFile) {
    try {
      return Files.readString(sourceFile);
    } catch (IOException exception) {
      throw new IllegalStateException("unable to read source file: " + sourceFile, exception);
    }
  }
}
