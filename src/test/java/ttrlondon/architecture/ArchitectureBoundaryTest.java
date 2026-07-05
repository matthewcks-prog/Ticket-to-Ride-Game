package ttrlondon.architecture;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Verifies architectural dependency boundaries that must not regress. */
final class ArchitectureBoundaryTest {
  private static final List<String> NON_UI_PACKAGES =
      List.of("domain", "application", "infrastructure");
  private static final List<String> FORBIDDEN_UI_IMPORTS =
      List.of("import javax.swing", "import java.awt");
  private static final Map<String, List<String>> FORBIDDEN_LAYER_IMPORTS =
      Map.of(
          "domain",
          List.of(
              "import ttrlondon.application",
              "import ttrlondon.infrastructure",
              "import ttrlondon.ui"),
          "application",
          List.of("import ttrlondon.infrastructure", "import ttrlondon.ui"),
          "infrastructure",
          List.of("import ttrlondon.application", "import ttrlondon.ui"));

  @Test
  void nonUiLayersDoNotImportSwingOrAwt() throws IOException {
    for (String packageName : NON_UI_PACKAGES) {
      Path sourceRoot = Path.of("src", "main", "java", "ttrlondon", packageName);

      List<Path> offendingFiles =
          Files.walk(sourceRoot)
              .filter(path -> path.toString().endsWith(".java"))
              .filter(ArchitectureBoundaryTest::containsForbiddenImport)
              .toList();

      assertTrue(
          offendingFiles.isEmpty(),
          () -> packageName + " must not import Swing/AWT: " + offendingFiles);
    }
  }

  @Test
  void layersOnlyDependInAllowedDirection() throws IOException {
    for (Map.Entry<String, List<String>> entry : FORBIDDEN_LAYER_IMPORTS.entrySet()) {
      Path sourceRoot = Path.of("src", "main", "java", "ttrlondon", entry.getKey());

      List<Path> offendingFiles =
          Files.walk(sourceRoot)
              .filter(path -> path.toString().endsWith(".java"))
              .filter(sourceFile -> containsAny(sourceFile, entry.getValue()))
              .toList();

      assertTrue(
          offendingFiles.isEmpty(),
          () -> entry.getKey() + " imports a forbidden layer: " + offendingFiles);
    }
  }

  private static boolean containsForbiddenImport(Path sourceFile) {
    return containsAny(sourceFile, FORBIDDEN_UI_IMPORTS);
  }

  private static boolean containsAny(Path sourceFile, List<String> forbiddenImports) {
    try {
      String source = Files.readString(sourceFile);
      return forbiddenImports.stream().anyMatch(source::contains);
    } catch (IOException exception) {
      throw new IllegalStateException("Could not read " + sourceFile, exception);
    }
  }
}
