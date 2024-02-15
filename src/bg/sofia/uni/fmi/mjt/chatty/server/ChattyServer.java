package bg.sofia.uni.fmi.mjt.chatty.server;

import bg.sofia.uni.fmi.mjt.chatty.server.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.chatty.server.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.chatty.server.command.CommandType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ChattyServer {

    private static final int BUFFER_SIZE = 1024;
    private static final String HOST = "localhost";

    private final CommandExecutor commandExecutor;

    private final Map<Map.Entry<String, String>, Set<SelectionKey>> openedPersonalChats;
    private final Map<String, Set<SelectionKey>> openedGroupChats;

    private final int port;
    private boolean isServerWorking;

    private ByteBuffer buffer;
    private Selector selector;

    public ChattyServer(int port, CommandExecutor commandExecutor) {
        openedPersonalChats = new LinkedHashMap<>();
        openedGroupChats = new LinkedHashMap<>();

        this.port = port;
        this.commandExecutor = commandExecutor;
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerWorking = true;

            System.out.println("Chatty Server is listening on port " + port + ".");

            while (isServerWorking) {
                try {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }

                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isReadable()) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            String clientInput = getClientInput(clientChannel);

                            if (clientInput == null) {
                                continue;
                            }

                            String[] inputTokens = clientInput.split(" ");

                            switch (CommandType.of(inputTokens[0])) {
                                case OPEN_CHAT -> registerChannelToChat(key, inputTokens);
                                case OPEN_GROUP -> registerChannelToGroup(key, inputTokens);
                                case CLOSE_CHAT -> removeChannelFromOpened(key, inputTokens);
                            }

                            String output = commandExecutor.execute(CommandCreator.newCommand(clientInput));
                            writeClientOutput(clientChannel, output);

                            if (CommandType.of(inputTokens[0]).equals(CommandType.SEND_MESSAGE)) {
                                updateChannelsInChat(inputTokens, output, key);
                            }

                        } else if (key.isAcceptable()) {
                            accept(selector, key);
                        }

                        keyIterator.remove();
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println(CommandExecutor.INCORRECT_FORMAT_MESSAGE);
                } catch (IOException e) {
                    System.out.println("Error occurred while processing client request: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("failed to start server", e);
        }
    }

    public void stop() {
        this.isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, this.port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();

        clientChannel.write(buffer);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel accept = sockChannel.accept();

        accept.configureBlocking(false);
        accept.register(selector, SelectionKey.OP_READ);
    }

    private void registerChannelToChat(SelectionKey key, String[] tokens) {
        if (openedPersonalChats.containsKey(Map.entry(tokens[1], tokens[2]))) {
            openedPersonalChats.get(Map.entry(tokens[1], tokens[2])).add(key);
        } else if (openedPersonalChats.containsKey(Map.entry(tokens[2], tokens[1]))) {
            openedPersonalChats.get(Map.entry(tokens[2], tokens[1])).add(key);
        } else {
            openedPersonalChats.put(Map.entry(tokens[1], tokens[2]), new LinkedHashSet<>(Set.of(key)));
        }
    }

    private void registerChannelToGroup(SelectionKey key, String[] tokens) {
        if (openedGroupChats.containsKey(tokens[1])) {
            openedGroupChats.get(tokens[1]).add(key);
        } else {
            openedGroupChats.put(tokens[1], new LinkedHashSet<>(Set.of(key)));
        }
    }

    private void removeChannelFromOpened(SelectionKey key, String[] tokens) {
        if (Integer.parseInt(tokens[tokens.length - 1]) == 1) {
            removeChannelFromChat(key, tokens);
        } else {
            removeChannelFromGroup(key, tokens);
        }
    }

    private void removeChannelFromChat(SelectionKey key, String[] tokens) {
        Map.Entry<String, String> chatKey;

        if (openedPersonalChats.containsKey(Map.entry(tokens[1], tokens[2]))) {
            chatKey = Map.entry(tokens[1], tokens[2]);
        } else {
            chatKey = Map.entry(tokens[2], tokens[1]);
        }

        openedPersonalChats.get(chatKey).remove(key);
    }

    private void removeChannelFromGroup(SelectionKey key, String[] tokens) {
        if (openedGroupChats.containsKey(tokens[1])) {
            openedGroupChats.get(tokens[1]).remove(key);
        }
    }

    private void updateChannelsInChat(String[] tokens, String message, SelectionKey currentKey) {
        Set<SelectionKey> channels;

        if (Integer.parseInt(tokens[tokens.length - 1]) == 1) {
            String leftUsername = tokens[tokens.length - 3];
            String rightUsername = tokens[tokens.length - 2];

            Map.Entry<String, String> chatKey;

            if (openedPersonalChats.containsKey(Map.entry(leftUsername, rightUsername))) {
                chatKey = Map.entry(leftUsername, rightUsername);
            } else {
                chatKey = Map.entry(rightUsername, leftUsername);
            }

            channels = openedPersonalChats.get(chatKey);
        } else {
            channels = openedGroupChats.get(tokens[tokens.length - 2]);
        }

        if (channels.isEmpty()) {
            return;
        }

        channels.stream().filter(c -> !c.equals(currentKey)).forEach(key -> {
            try {
                writeClientOutput((SocketChannel) key.channel(), message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
