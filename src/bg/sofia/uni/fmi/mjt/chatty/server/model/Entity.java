package bg.sofia.uni.fmi.mjt.chatty.server.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface Entity {

    static <T extends Entity> T loadFrom(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        return Entity.cast(stream.readObject());
    }

    default void saveTo(ObjectOutputStream writer) throws IOException {
        writer.writeObject(this);
    }

    private static <T extends Entity> T cast(Object object) {
        return ((T) object);
    }

}
