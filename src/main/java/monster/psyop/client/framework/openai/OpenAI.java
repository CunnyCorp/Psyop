package monster.psyop.client.framework.openai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OpenAI {
    public final String apiKey;
    private final Gson gson = new GsonBuilder().create();
    private final HttpClient client = HttpClient.newHttpClient();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    // A cache to store the response for each request
    private final Map<CompletionBody, CompletionResponse> responseCache = new ConcurrentHashMap<>();

    public OpenAI(String apiKey) {
        this.apiKey = apiKey;
    }

    public void requestCompletion(CompletionBody body, AiResponse function) {
        // Check if the response is already in the cache
        if (responseCache.containsKey(body)) {
            function.run(responseCache.get(body));
            return;
        }

        CompletableFuture.supplyAsync(
                        () -> {
                            HttpRequest request =
                                    HttpRequest.newBuilder()
                                            .uri(URI.create("https://api.openai.com/v1/completions"))
                                            .header("Authorization", "Bearer " + apiKey)
                                            .header("Content-Type", "application/json")
                                            .POST(
                                                    HttpRequest.BodyPublishers.ofString(
                                                            gson.toJson(body, CompletionBody.class)))
                                            .build();
                            try {
                                HttpResponse<String> response =
                                        client.send(request, HttpResponse.BodyHandlers.ofString());
                                return gson.fromJson(response.body(), CompletionResponse.class);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        },
                        executorService)
                .thenAccept(
                        response -> {
                            if (response != null) {
                                responseCache.put(body, response);
                                function.run(response);
                            }
                        });
    }

    @FunctionalInterface
    public interface AiResponse {
        void run(CompletionResponse response);
    }
}
