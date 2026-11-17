package lab.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LabCatalogTest {
    @TempDir Path root;

    @Test
    void discoversSectionsAndReadsSourceMetadata() throws Exception {
        Path section = Files.createDirectory(root.resolve("13-version-features"));
        Files.writeString(section.resolve("Future.java"), "/*\n * Challenge 117: Future Lab\n"
                + " * Run on JDK 25+: java Future.java\n */\nclass Future {}\n");
        Files.writeString(root.resolve("Scratchpad.java"), "class Scratchpad {}\n");

        var found = new LabCatalog(root).discover();

        assertEquals(2, found.size());
        assertEquals("117  Future Lab", found.get("13-version-features").getFirst().title());
        assertEquals(25, found.get("13-version-features").getFirst().requiredJdk());
        assertEquals(Path.of("Scratchpad.java"), found.get("Scratchpad").getFirst().source());
        assertTrue(found.get("13-version-features").getFirst().source().toString().endsWith("Future.java"));
    }
}
