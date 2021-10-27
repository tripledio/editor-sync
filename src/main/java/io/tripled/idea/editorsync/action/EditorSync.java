package io.tripled.idea.editorsync.action;

import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

public class EditorSync {

    private String currentEditor;
    private List<String> openEditors;

    public static EditorSync loadFrom(Path path) throws IOException {
        var reader = new GsonBuilder().create();

        try (FileReader jsonReader = new FileReader(path.resolve(".editorSync.json").toFile())) {
            return reader.fromJson(jsonReader, EditorSync.class);
        }
    }

    public EditorSync(String currentEditor, List<String> openEditors) {
        this.currentEditor = currentEditor;
        this.openEditors = openEditors;
    }

    public String getCurrentEditor() {
        return currentEditor;
    }

    public List<String> getOpenEditors() {
        return openEditors;
    }

    public void write(Path path) throws IOException {
        try (Writer fileWriter = new FileWriter(path.resolve(".editorSync.json").toString())) {
            var writer = new GsonBuilder().setPrettyPrinting()
                    .create();

            writer.toJson(this, fileWriter);
        }
    }

    @Override
    public String toString() {
        return "EditorSync{" +
                "currentEditor='" + currentEditor + '\'' +
                ", openEditors=" + openEditors +
                '}';
    }
}
