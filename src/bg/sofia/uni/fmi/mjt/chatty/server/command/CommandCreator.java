package bg.sofia.uni.fmi.mjt.chatty.server.command;

import java.util.Arrays;
import java.util.List;

public class CommandCreator {

    public static Command newCommand(String input) {
        List<String> tokens = CommandCreator.getTokens(input);
        String[] args = tokens.subList(1, tokens.size()).toArray(new String[0]);

        return new Command(tokens.get(0), args);
    }

    private static List<String> getTokens(String input) {
        return Arrays.stream(input.split(" ")).toList();
    }

}
