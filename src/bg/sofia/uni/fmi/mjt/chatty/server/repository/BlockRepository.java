package bg.sofia.uni.fmi.mjt.chatty.server.repository;


import bg.sofia.uni.fmi.mjt.chatty.server.model.Block;

public class BlockRepository extends Repository<Block> {

    private static final String DB_PATH = "";

    private static final BlockRepository instance = new BlockRepository(DB_PATH);

    private BlockRepository(String path) {
        super(path);
    }

    public static BlockRepository getInstance() {
        return instance;
    }

}
