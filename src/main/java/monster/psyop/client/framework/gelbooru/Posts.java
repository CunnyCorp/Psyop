package monster.psyop.client.framework.gelbooru;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Posts {
    public List<Post> post = new ArrayList<>();

    @SuppressWarnings("unused")
    public static class Post {
        public int id;

        @SerializedName("created_at")
        public String createdAt;

        public int score;
        public int width, height;
        public String md5;
        public String directory;
        public String image;
        public String rating;
        public String source;
        public int change;
        public String owner;

        @SerializedName("creator_id")
        public int creatorId;

        @SerializedName("parent_id")
        public int parentId;

        public int sample;

        @SerializedName("preview_height")
        public int previewHeight;

        @SerializedName("preview_width")
        public int previewWidth;

        public String tags;
        public String title;

        @SerializedName("has_notes")
        public boolean hasNotes;

        @SerializedName("has_comments")
        public boolean hasComments;

        @SerializedName("file_url")
        public String fileUrl;

        @SerializedName("preview_url")
        public String previewUrl;

        @SerializedName("sample_url")
        public String sampleUrl;

        @SerializedName("sample_height")
        public int sampleHeight;

        @SerializedName("sample_width")
        public int sampleWidth;

        public String status;

        @SerializedName("post_locked")
        public int postLocked;

        @SerializedName("has_children")
        public boolean hasChildren;
    }
}
