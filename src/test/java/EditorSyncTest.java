import io.tripled.idea.editorsync.action.EditorSync;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class EditorSyncTest {

    @TempDir
    Path tempDir;

    @Test
    void canLoad() throws IOException {
        Files.write(tempDir.resolve(".editorSync.json"), "{\"currentEditor\": \"currentEditor\", \"openEditors\": [\"GildedRose.java\", \"Item.java\"]}".getBytes(StandardCharsets.UTF_8));

        var editorSync = EditorSync.loadFrom(tempDir);

        assertThat(editorSync.getOpenEditors()).containsExactly("GildedRose.java", "Item.java");
        assertThat(editorSync.getCurrentEditor()).isEqualTo("currentEditor");
    }

    @Test
    void canWrite() throws IOException {
        var editorSync = new EditorSync("currentEditor", Collections.singletonList("Item.java"));

        editorSync.write(tempDir);

        assertThat(Files.readString(tempDir.resolve(".editorSync.json"))).isEqualTo("{\n" +
                "  \"currentEditor\": \"currentEditor\",\n" +
                "  \"openEditors\": [\n" +
                "    \"Item.java\"\n" +
                "  ]\n" +
                "}");
    }
}
