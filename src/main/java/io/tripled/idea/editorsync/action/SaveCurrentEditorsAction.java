package io.tripled.idea.editorsync.action;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class SaveCurrentEditorsAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        var project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project currentProject = event.getProject();
        try {
            var parent = ofNullable(currentProject)
                    .map(Project::getProjectFile)
                    .map(VirtualFile::getParent)
                    .map(VirtualFile::getParent)
                    .orElseThrow();

            var allEditors = FileEditorManager.getInstance(currentProject).getAllEditors();
            var editorSync = new EditorSync(null, toList(allEditors));

            editorSync.write(Path.of(parent.getPath()));

            parent.refresh(true, true);
        } catch (IOException e) {
            NotificationGroupManager.getInstance().getNotificationGroup("EditorSync Notification Group")
                    .createNotification("Oops, could not save editors for some reason!", NotificationType.ERROR)
                    .notify(currentProject);
            e.printStackTrace();
        }
    }

    @NotNull
    private List<String> toList(FileEditor[] allEditors) {
        return Arrays.stream(allEditors)
                .map(FileEditor::getFile)
                .map(VirtualFile::getName)
                .collect(Collectors.toList());
    }
}
