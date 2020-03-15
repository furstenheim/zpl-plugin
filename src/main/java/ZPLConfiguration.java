import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ZPLConfiguration {
    static Map<String, ZPLCommand> commands;
    public static Map<String, ZPLCommand> getCommands () throws IOException {
        if (commands != null) {
            return commands;
        }
        ClassLoader classLoader = ZPLConfiguration.class.getClassLoader();
        URL resource = classLoader.getResource("zpl.json");
        try {
            System.out.println(resource);
            JsonParser jsonParser = new JsonParser();
            JsonElement configuration = jsonParser.parse(new InputStreamReader(resource.openStream()));
            JsonArray commandsAsJson = configuration.getAsJsonArray();
            Gson gson = new Gson();
            commands = StreamSupport.stream(commandsAsJson.spliterator(), false)
                    .map(command -> gson.fromJson(command, ZPLCommand.class))
                    .collect(Collectors.toMap(ZPLCommand::getCode, Function.identity()));
            return commands;
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }
}
