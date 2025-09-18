package monster.psyop.client.framework.gelbooru;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

// CHAOS, MAY THE PEOPLE KNOW THE FEAR OF RESIZING IMAGES
public class GelbooruUtil {
    @Getter
    @Setter
    private static Posts.Post post;
    private GelbooruScraper.ImageType imageType = GelbooruScraper.ImageType.Direct;
    private int maxSize = 256;

    public void setType(GelbooruScraper.ImageType type) {
        this.imageType = type;
    }

    public void setMaxSize(int i) {
        this.maxSize = i;
    }

    public InputStream getImage() {
        try {
            return new URL(getUrl()).openStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUrl() {
        return switch (imageType) {
            case Sample -> post.sampleUrl;
            case Preview -> post.previewUrl;
            case Direct -> post.fileUrl;
        };
    }

    private int getHeight() {
        return switch (imageType) {
            case Sample -> post.sampleHeight;
            case Preview -> post.previewHeight;
            case Direct -> post.height;
        };
    }

    private int getWidth() {
        return switch (imageType) {
            case Sample -> post.sampleWidth;
            case Preview -> post.previewWidth;
            case Direct -> post.width;
        };
    }

    private double getWidthRatio() {
        int width = getWidth();
        int height = getHeight();
        return (double) width / height;
    }

    private double getHeightRatio() {
        int width = getWidth();
        int height = getHeight();
        return (double) height / width;
    }

    private int proxyHeightFix() {
        return (int) Math.round(getHeightRatio() * getFixedWidth());
    }

    public int getFixedHeight() {
        return (getWidth() > maxSize || getHeight() > maxSize) ? proxyHeightFix() : getHeight();
    }

    public int getFixedWidth() {
        return Math.toIntExact(
                getWidth() > maxSize || getHeight() > maxSize
                        ? Math.round(getWidthRatio() * maxSize)
                        : getWidth());
    }
}
