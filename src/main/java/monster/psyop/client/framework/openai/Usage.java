package monster.psyop.client.framework.openai;

import com.google.gson.annotations.SerializedName;

// {"prompt_tokens":42,"completion_tokens":13,"total_tokens":55}
public class Usage {
    @SerializedName("prompt_tokens")
    public int promptTokens;

    @SerializedName("completion_tokens")
    public int completionTokens;

    @SerializedName("total_tokens")
    public int totalTokens;
}
