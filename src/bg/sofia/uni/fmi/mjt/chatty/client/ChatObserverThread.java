package bg.sofia.uni.fmi.mjt.chatty.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ChatObserverThread extends Thread {

    private final SocketChannel channel;

    private final ChatState chatState;

    private final ByteBuffer buffer;

    public ChatObserverThread(SocketChannel channel, ChatState chatState, ByteBuffer buffer) {
        this.channel = channel;
        this.chatState = chatState;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        while (!chatState.equals(ChatState.NOT_IN_CHAT)) {
            try {
                buffer.clear();
                channel.read(buffer);

                buffer.flip();

                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);

                String reply = new String(byteArray, StandardCharsets.UTF_8);

                if (reply.equals("closed")) {
                    System.out.println("Chat closed");
                    break;
                }

                if (!reply.startsWith("[")) {
                    continue;
                }

                System.out.println(reply);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
