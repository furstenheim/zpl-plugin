import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.ide.ui.EditorOptionsTopHitProvider;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PopupDialogAction extends AnAction {
    static Map<String, ZPLCommand> commands;
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
        Map<String, ZPLCommand> commands;
        try {
            commands = getCommands();
        } catch (IOException ex) {
            HintManager.getInstance().showErrorHint(editor, "Could not load plugin");
            return;
        }
        System.out.println(commands);

    }

    private Map<String, ZPLCommand> getCommands () throws IOException {
        if (commands != null) {
            return commands;
        }
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("zpl.json");
        try {
            System.out.println(resource);
            JsonParser jsonParser = new JsonParser();
            JsonElement configuration = jsonParser.parse(new InputStreamReader(resource.openStream()));
            JsonArray commands = configuration.getAsJsonArray();
            Gson gson = new Gson();
            return StreamSupport.stream(commands.spliterator(), false)
                    .map(command -> gson.fromJson(command, ZPLCommand.class))
                    .collect(Collectors.toMap(ZPLCommand::getCode, Function.identity()));

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }
}

