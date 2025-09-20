package monster.psyop.client.utility;

import monster.psyop.client.Psyop;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystem {
    public static String getUrl(String url) {
        String response = null;
        try (InputStream s = new URL(url).openStream()) {
            response = new String(s.readAllBytes());
        } catch (IOException ignored) {
        }
        return response;
    }

    public static void write(Path path, String value) {
        mkdir(path.getParent());

        try {
            Files.write(path, value.getBytes());
        } catch (IOException e) {
            Psyop.LOG.info(path + " failed to write.", e);
        }
    }

    public static void mkdir(Path path) {
        if (Files.exists(path)) return;

        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            Psyop.LOG.info(path + " failed to create directories.", e);
        }
    }
}
