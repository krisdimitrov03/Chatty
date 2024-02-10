package bg.sofia.uni.fmi.mjt.chatty.server.model;

public record User(String firstName, String lastName, String username, String passwordHash) implements Entity {

    public String getFullName() {
        return firstName() + " " + lastName();
    }

}
