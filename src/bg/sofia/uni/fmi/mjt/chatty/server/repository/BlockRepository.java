package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.Block;

import java.io.InputStream;

public class BlockRepository extends Repository<Block> {

    private static final String DB_PATH = "blocks.dat";

    private static BlockRepository instance;

    private BlockRepository(String path) {
        super(path);
    }

    private BlockRepository(InputStream stream) {
        super(stream);
    }

    public static BlockRepository getInstance() {
        if (instance == null) {
            instance = new BlockRepository(DB_PATH);
        }

        return instance;
    }

    public static BlockRepository getInstance(InputStream stream) {
        if (instance == null) {
            instance = new BlockRepository(stream);
        }

        return instance;
    }

}
