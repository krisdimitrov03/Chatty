package bg.sofia.uni.fmi.mjt.chatty.server.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public interface Entity extends Serializable {

    static <T extends Entity> T loadFrom(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        return Entity.cast(stream.readObject());
    }

    default void saveTo(ObjectOutputStream writer) {
        try {
            writer.writeObject(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T extends Entity> T cast(Object object) {
        return ((T) object);
    }

}
