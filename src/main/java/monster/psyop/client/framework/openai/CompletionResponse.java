package monster.psyop.client.framework.openai;

import java.util.List;

/*
{"id":"cmpl-6k43OD5xilBiBUAHQWSj8Oue55o4O",
"object":"text_completion",
"created":1676436690,
"model":"text-davinci-003",
"choices":[{"text":"ida\n\nYes, I'm working now, Vida!","index":0,"logprobs":null,"finish_reason":"stop"}],
"usage":{"prompt_tokens":42,"completion_tokens":13,"total_tokens":55}}
 */
public class CompletionResponse {
    public String id;
    public String object;
    public long created;
    public String model;
    public List<Choice> choices;
    public Usage usage;
}
