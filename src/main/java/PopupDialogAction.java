import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

public class PopupDialogAction extends AnAction {
    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Project project = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        System.out.println("Aaaa");
        if (editor == null) {
            return;
        }
        LogicalPosition position = editor.getCaretModel().getPrimaryCaret().getLogicalPosition();
        int offset = editor.getCaretModel().getPrimaryCaret().getOffset();
        LogicalPosition startOfLine = new LogicalPosition(position.line, 0);
        Document document = editor.getDocument();
        int lineStartOffset = document.getLineStartOffset(position.line);
        document.getText(new TextRange(lineStartOffset, offset));
        // HintManager.getInstance().showErrorHint(editor, "AAA");
        HintManager.getInstance().showInformationHint(editor, "AAAA");
    }
}

