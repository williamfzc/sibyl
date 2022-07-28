package com.williamfzc.sibyl.ext.casegen.idea;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class ConsoleToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        SimpleToolWindowPanel panel = new SimpleToolWindowPanel(false, true);
        toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(panel, "", false));
    }
}