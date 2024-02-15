package bg.sofia.uni.fmi.mjt.chatty.client;

public enum ChatState {
    PERSONAL,
    GROUP,
    NOT_IN_CHAT;

    public int getIntValue() {
        return this.equals(PERSONAL) ? 1 : this.equals(GROUP) ? 2 : 3;
    }
}
