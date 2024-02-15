package bg.sofia.uni.fmi.mjt.chatty.server.repository;

import bg.sofia.uni.fmi.mjt.chatty.server.model.Block;

public class BlockRepository extends Repository<Block> {

    private static final String DB_PATH = "blocks.dat";

    private static BlockRepository instance;

    private BlockRepository(String path) {
        super(path);
    }

    public static BlockRepository getInstance() {
        if (instance == null) {
            instance = new BlockRepository(DB_PATH);
        }

        return instance;
    }

}
