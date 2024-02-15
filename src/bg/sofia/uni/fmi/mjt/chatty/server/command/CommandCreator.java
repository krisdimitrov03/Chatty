package bg.sofia.uni.fmi.mjt.chatty.server.command;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CommandCreator {

    private static final int MAX_SPLIT_BY_QUOTE_TOKENS_COUNT = 3;

    public static Command newCommand(String input) {
        List<String> tokens = getTokens(input);
        String[] args = tokens.subList(1, tokens.size()).toArray(new String[0]);

        return new Command(tokens.getFirst(), args);
    }

    private static List<String> getTokens(String input) {
        if (!input.startsWith(CommandType.SEND_MESSAGE.toString())) {
            return Arrays.stream(input.split(" ")).toList();
        }

        String[] tokensByQuote = input.split("\"");

        if (tokensByQuote.length != MAX_SPLIT_BY_QUOTE_TOKENS_COUNT) {
            throw new IllegalArgumentException("Incorrect command format");
        }

        String message = tokensByQuote[1];

        String[] noMessageTokens = input.replace(" \"" + message + "\"", "").split(" ");

        List<String> result = new LinkedList<>(List.of(noMessageTokens));
        result.add(1, message);

        return result;
    }

}
