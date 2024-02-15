package bg.sofia.uni.fmi.mjt.chatty.client;

public enum ChatState {

    PERSONAL(1),
    GROUP(2),
    NOT_IN_CHAT(3);

    private final int value;

    ChatState(int value) {
        this.value = value;
    }

    public int getIntValue() {
        return value;
    }

}
