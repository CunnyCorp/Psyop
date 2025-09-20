package monster.psyop.client.framework.gelbooru;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import monster.psyop.client.Psyop;
import monster.psyop.client.utility.FileSystem;

import java.util.ArrayList;

public class GelbooruScraper {
    private static final String BASE_URL =
            "https://gelbooru.com/index.php?page=dapi&s=post&q=index&json=1&limit=100";
    public Gson gson;
    public Posts posts;
    public String rawTags = "";
    public String rawExcludedTags = "";
    private int page = 0;
    private int currentImage = 0;
    private String rating = "";
    private String tags = "";

    public GelbooruScraper() {
        this.gson = new GsonBuilder().create();
    }

    public boolean load() {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);
        urlBuilder.append("&tags=").append(tags);

        if (!rating.isEmpty()) {
            urlBuilder.append("+rating%3a").append(rating);
        }

        urlBuilder.append("&pid=").append(page);

        String response = FileSystem.getUrl(urlBuilder.toString());

        if (response == null || response.equals("Too deep! Pull it back some. Holy fuck.")) {
            page = 0;
            return false;
        }

        this.posts = gson.fromJson(response, Posts.class);

        if (this.posts.post.isEmpty()) {
            page = 0;
            return false;
        }

        boolean isCurrentPostInvalid = isInvalid();

        if (isCurrentPostInvalid) {
            nextPost();
        }

        page++;

        return !isCurrentPostInvalid;
    }

    public boolean isInvalid() {
        String file = posts.post.get(currentImage).image;
        return !file.endsWith(".png") && !file.endsWith(".jpg") && !file.endsWith(".jpeg");
    }

    public void setTags(String str, String excl) {
        rawExcludedTags = excl;
        var newTags = new ArrayList<String>();
        for (String tag : excl.split(" ")) {
            newTags.add("-" + tag.toLowerCase());
        }

        rawTags = str;
        tags = String.join("+", str.split(" ")).toLowerCase();
        if (!newTags.isEmpty()) tags += (tags.isBlank() ? "" : "+") + String.join("+", newTags);
        Psyop.LOG.info(tags);
    }

    public void nextPost() {
        currentImage++;
        if (currentImage >= get().post.size() - 1 || posts.post.isEmpty()) {
            load();
        } else {
            if (isInvalid()) nextPost();
        }

        GelbooruUtil.setPost(getPost());
    }

    public void reset() {
        this.page = 0;
        this.currentImage = 0;
    }

    public Posts.Post getPost() {
        return posts.post.get(currentImage);
    }

    public Posts get() {
        return posts;
    }

    public void setRating(Rating r) {
        rating = r.name().toLowerCase();
    }

    @SuppressWarnings("unused")
    public enum Rating {
        Explicit,
        Questionable,
        Sensitive,
        General,
        None
    }

    public enum ImageType {
        Sample,
        Preview,
        Direct
    }
}
