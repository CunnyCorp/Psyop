package monster.psyop.client.framework.gelbooru;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import monster.psyop.client.Psyop;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GelbooruScraper {
    private static final String BASE_URL =
            "https://gelbooru.com/index.php?page=dapi&s=post&q=index&json=1&limit=100";
    public Gson gson;
    public Posts posts;
    public String rawTags = "";
    public String rawExcludedTags = "";
    public String apiCredentials;
    private int page = 0;
    private int currentImage = 0;
    private String tags = "";

    public GelbooruScraper() {
        this.gson = new GsonBuilder().create();
    }

    public boolean load() {
        String urlBuilder = BASE_URL + apiCredentials + "&tags=" + tags +
                "&pid=" + page;

        String response = fetchUrlContent(urlBuilder);

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

        Psyop.LOG.info("Loaded page " + page);

        return !isCurrentPostInvalid;
    }

    private String fetchUrlContent(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:141.0) Gecko/20100101 Firefox/141.0");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                Psyop.LOG.warn("HTTP request failed with response code: " + responseCode);
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } catch (Exception e) {
            Psyop.LOG.warn("Failed to fetch URL content: " + e.getMessage());
            return null;
        }
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