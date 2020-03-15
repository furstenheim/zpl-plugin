import com.intellij.codeInsight.documentation.DocumentationComponent;
import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.TextRange;
import com.intellij.ui.popup.PopupFactoryImpl;
import com.intellij.util.ObjectUtils;
import com.sun.istack.NotNull;
import org.assertj.core.util.Strings;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ZPLPopup {
    public static void showPopup (@NotNull AnActionEvent e, boolean showFullPopup) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }

        Map<String, ZPLCommand> commands;
        try {
            commands = ZPLConfiguration.getCommands();
        } catch (IOException ex) {
            HintManager.getInstance().showErrorHint(editor, "Could not load plugin");
            return;
        }


        LogicalPosition position = editor.getCaretModel().getPrimaryCaret().getLogicalPosition();
        int offset = editor.getCaretModel().getPrimaryCaret().getOffset();
        Document document = editor.getDocument();
        int lineStartOffset = document.getLineStartOffset(position.line);
        int lineEndOffset = document.getLineEndOffset(position.line);
        String text = document.getText(new TextRange(lineStartOffset, Math.min(lineEndOffset, offset + 3)));

        for (int i = Math.min(text.length() - 3, offset - lineStartOffset); i >= 0; i--) {
            char c = text.charAt(i);
            if (c == '~' || c == '^') {
                String code = text.substring(i, i + 3);
                ZPLCommand zplCommand = commands.get(code);
                if (zplCommand == null) {
                    HintManager.getInstance().showInformationHint(editor, "Could not find command");
                    return;
                }
                String definition = zplCommand.getDefinition();
                if (showFullPopup) {
                    final Project project = e.getProject();
                    DocumentationManager documentationManager = DocumentationManager.getInstance(project);
                    final DocumentationComponent component = new DocumentationComponent(documentationManager);
                    List<String> parameters = zplCommand.getParameters();
                    String parametersText = parameters.stream()
                            .map(p -> escapeHtml(p))
                            .map(p -> p.replaceAll(" ", "&nbsp;"))
                            .collect(Collectors.joining("<br>"));
                    System.out.println(parametersText);
                    component.setText(String.format("<code><b>%s&nbsp;</b>&nbsp;&nbsp;%s<br><b>Format: </b>%s<br><br><b>Description: </b>%s<br><br><b>Parameters: </b><br>%s</code>", zplCommand.getCode(), zplCommand.getDefinition(),
                                                    String.join("<br>", zplCommand.getFormat()), String.join("<br>", zplCommand.getDescription()), parametersText), null, null);
                    JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(component, null)
                            .setProject(project)
                            .setDimensionServiceKey(project, "#####", false)
                            .setResizable(true)
                            .setMovable(true)
                            .createPopup();
                    Disposer.register(popup, component);

                    popup.showInBestPositionFor(e.getDataContext());
                } else {
                    HintManager.getInstance().showInformationHint(editor, String.format("%s %s", code, definition));
                }
                return;
            }
        }
        HintManager.getInstance().showInformationHint(editor, "Could not find command");
    }
}
