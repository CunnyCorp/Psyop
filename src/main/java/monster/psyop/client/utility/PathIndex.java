package monster.psyop.client.utility;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathIndex {
    public static final Path CLIENT = Paths.get(System.getProperty("user.dir")).resolve("shondo");
    public static final Path LOGS = CLIENT.resolve("logs");
    public static final Path FONTS = CLIENT.resolve("fonts");
    public static final Path WORLD_SAVE = CLIENT.resolve("world_saving");
    public static final Path COMPATIBILITY = CLIENT.resolve("compatibility");
    public static final Path GELBOORU = CLIENT.resolve("gelbooru");
    public static final Path MAPS = CLIENT.resolve("maps");

    // Files
    public static final Path CONFIG = CLIENT.resolve("config.json");
}
