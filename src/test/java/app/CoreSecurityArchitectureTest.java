package app;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class CoreSecurityArchitectureTest {

    @Test
    void coreSecuritySourcesDoNotDependOnDomainClasses() throws IOException {
        Path securitySources = Path.of("src", "main", "java", "app", "core", "security");
        List<Path> javaSources;
        try (var paths = Files.walk(securitySources)) {
            javaSources = paths.filter(path -> path.toString().endsWith(".java")).toList();
        }

        assertThat(javaSources).isNotEmpty();
        for (Path sourceFile : javaSources) {
            assertThat(Files.readString(sourceFile))
                    .as(sourceFile.toString())
                    .doesNotContain("app.domain");
        }
    }
}
