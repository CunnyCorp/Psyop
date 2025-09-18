package monster.psyop.client.framework.openai;

import com.google.gson.annotations.SerializedName;

public class CompletionBody {
    public String model;
    public String prompt;

    @SerializedName("max_tokens")
    public int maxTokens;

    public double temperature;

    public CompletionBody(Builder builder) {
        this.model = builder.model;
        this.prompt = builder.prompt;
        this.maxTokens = builder.maxTokens;
        this.temperature = builder.temperature;
    }

    public static class Builder {
        private String model = "text-davinci-003";
        private String prompt = "test prompt";
        private int maxTokens = 32;
        private double temperature = 0;

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public Builder maxTokens(int i) {
            this.maxTokens = i;
            return this;
        }

        public Builder temperature(double i) {
            this.temperature = i;
            return this;
        }

        public CompletionBody build() {
            return new CompletionBody(this);
        }
    }
}
