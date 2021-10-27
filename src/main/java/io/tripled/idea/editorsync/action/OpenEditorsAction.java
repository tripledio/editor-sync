package io.tripled.idea.editorsync.action;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScopes;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import static java.util.Optional.of;

public class OpenEditorsAction extends AnAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenEditorsAction.class);

    @Override
    public void update(@NotNull AnActionEvent e) {
        var project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var currentProject = Objects.requireNonNull(event.getProject());
        var fileEditorManager = Objects.requireNonNull(FileEditorManager.getInstance(currentProject));
        try {
            Arrays.stream(fileEditorManager.getOpenFiles())
                    .forEach(fileEditorManager::closeFile);

            var parent = of(currentProject)
                    .map(Project::getProjectFile)
                    .map(VirtualFile::getParent)
                    .map(VirtualFile::getParent)
                    .orElseThrow();

            var editorSync = EditorSync.loadFrom(Path.of(parent.getPath()));

            editorSync.getOpenEditors().stream()
                    .flatMap(fileName -> FilenameIndex.getVirtualFilesByName(fileName, GlobalSearchScopes.projectProductionScope(currentProject)).stream())
                    .map(virtualFile -> new OpenFileDescriptor(currentProject, virtualFile, 0))
                    .forEach(desc -> fileEditorManager.openTextEditor(desc, true));

        } catch (IOException e) {
            LOGGER.debug("Could not load saved editors", e);
            NotificationGroupManager.getInstance().getNotificationGroup("EditorSync Notification Group")
                    .createNotification("No Editors loaded because .editorSync could not be opened.", NotificationType.ERROR)
                    .notify(currentProject);
        }
    }
}
