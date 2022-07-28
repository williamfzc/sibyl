package com.williamfzc.sibyl.ext.casegen.idea;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class ConfigDialog extends DialogWrapper {
    private static final String NAME_TOOL_WINDOW = "sibyl_tool_window";
    private static final String NAME_CONSOLE_TAB = "sibyl output";

    protected ConfigDialog(@Nullable Project project) {
        super(project);
        init();

        Objects.requireNonNull(getButton(myOKAction)).addActionListener(e -> {
            ToolWindow toolWindow = createToolWindow(project);
            // todo: start sibyl
            // https://intellij-support.jetbrains.com/hc/en-us/community/posts/206756385-How-to-make-a-simple-console-output
        });
    }

    private ToolWindow createToolWindow(Project project) {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow(NAME_TOOL_WINDOW);
        ContentManager contentManager = toolWindow.getContentManager();
        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        Content content = contentManager.getFactory().createContent(consoleView.getComponent(), NAME_CONSOLE_TAB, false);
        contentManager.addContent(content);
        contentManager.setSelectedContent(content);
        toolWindow.show();
        return toolWindow;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new GridLayout(4, 3));

        JLabel caseLabel = new JLabel("case dir", JLabel.LEFT);
        JLabel caseChosenLabel = new JLabel("", JLabel.LEFT);
        JButton caseButton = new JButton("choose dir");
        caseButton.addActionListener(e -> {
            JFileChooser fileChooser = createFileChooser();
            int ret = fileChooser.showOpenDialog(dialogPanel);
            if (ret == JFileChooser.APPROVE_OPTION) {
                caseChosenLabel.setText(fileChooser.getSelectedFile().getPath());
            }
        });

        JLabel srcLabel = new JLabel("source code dir", JLabel.LEFT);
        JLabel srcChosenLabel = new JLabel("", JLabel.LEFT);
        JButton srcButton = new JButton("choose dir");
        srcButton.addActionListener(e -> {
            JFileChooser fileChooser = createFileChooser();
            int ret = fileChooser.showOpenDialog(dialogPanel);
            if (ret == JFileChooser.APPROVE_OPTION) {
                srcChosenLabel.setText(fileChooser.getSelectedFile().getPath());
            }
        });

        JLabel outputLabel = new JLabel("output dir", JLabel.LEFT);
        JLabel outputChosenLabel = new JLabel("", JLabel.LEFT);
        JButton outputButton = new JButton("choose dir");
        outputButton.addActionListener(e -> {
            JFileChooser fileChooser = createFileChooser();
            int ret = fileChooser.showOpenDialog(dialogPanel);
            if (ret == JFileChooser.APPROVE_OPTION) {
                outputChosenLabel.setText(fileChooser.getSelectedFile().getPath());
            }
        });

        dialogPanel.add(caseLabel);
        dialogPanel.add(caseChosenLabel);
        dialogPanel.add(caseButton);
        dialogPanel.add(srcLabel);
        dialogPanel.add(srcChosenLabel);
        dialogPanel.add(srcButton);
        dialogPanel.add(outputLabel);
        dialogPanel.add(outputChosenLabel);
        dialogPanel.add(outputButton);

        return dialogPanel;
    }

    private JFileChooser createFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return fileChooser;
    }
}
