package me.drkapdor.openai4j;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import me.drkapdor.openai4j.completions.CompletionModel;
import me.drkapdor.openai4j.completions.request.CompletionRequest;
import me.drkapdor.openai4j.completions.response.CompletionResponse;
import me.drkapdor.openai4j.completions.response.ResponseUsage;
import me.drkapdor.openai4j.completions.response.TextVariation;
import me.drkapdor.openai4j.images.request.ImageRequest;
import me.drkapdor.openai4j.images.response.GeneratedImage;
import me.drkapdor.openai4j.images.response.ImageResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Клиент для взаимодействия с сервисами OpenAI
 * @author DrKapdor
 */
public class OpenAiClient {

    private static final String COMPLETIONS_URL = "https://api.openai.com/v1/completions";
    private static final String IMAGES_URL = "https://api.openai.com/v1/images/generations";

    private final String accessToken;
    private final CloseableHttpClient httpClient;
    private final ExecutorService executorService;

    /**
     * Конструктор клиента
     * @param accessToken Токен доступа к сервисам OpenAI
     */
    public OpenAiClient(String accessToken) {
        this.accessToken = accessToken;
        httpClient = HttpClients.createDefault();
        executorService = Executors.newFixedThreadPool(16);
    }

    /**
     * Генерирует изображение в соответствии с указанным описанием
     * @param request Запрос, содержащий параметры желаемого изображения
     * @param callback Функция, вызываемая после получения ответа от сервиса
     */

    public void generateImages(ImageRequest request, Consumer<ImageResponse> callback) {
        HttpPost httpPost = new HttpPost(IMAGES_URL);
        httpPost.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        httpPost.addHeader("Authorization", "Bearer " + accessToken);
        httpPost.setEntity(new StringEntity(request.toJson().toString(), ContentType.APPLICATION_JSON));
        CompletableFuture.supplyAsync(() -> {
            try {
                CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
                JsonObject jsonResponse = JsonParser.parseReader(
                        new JsonReader(
                                new InputStreamReader(httpResponse.getEntity().getContent()))
                ).getAsJsonObject();
                List<GeneratedImage> images = new ArrayList<>();
                JsonArray jsonDataArray = jsonResponse.get("data").getAsJsonArray();
                for (JsonElement jsonData : jsonDataArray) {
                    JsonObject jsonDataObject = jsonData.getAsJsonObject();
                    images.add(new GeneratedImage(jsonDataObject.get(request.getResponseFormat().toString()).getAsString()));
                }
                return ImageResponse.builder()
                        .creationDate(jsonResponse.get("created").getAsLong())
                        .user(jsonResponse.has("user") ? jsonResponse.get("user").getAsString() : null)
                        .images(images).build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executorService).thenAcceptAsync(callback);
    }

    /**
     * Генерирует ответ на основе сформулированного ответа
     * @param request Запрос, содержащий параметры желаемого ответа
     * @param callback Функция, вызываемая после получения ответа от сервиса
     */

    public void generateCompletions(CompletionRequest request, Consumer<CompletionResponse> callback) {
        HttpPost httpGet = new HttpPost(COMPLETIONS_URL);
        httpGet.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        httpGet.addHeader("Authorization", "Bearer " + accessToken);
        httpGet.setEntity(new StringEntity(request.toJson().toString(), ContentType.APPLICATION_JSON));
        CompletableFuture.supplyAsync(() -> {
            try {
                CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
                JsonObject jsonResponse = JsonParser.parseReader(
                        new JsonReader(new InputStreamReader(httpResponse.getEntity().getContent()))
                ).getAsJsonObject();
                List<TextVariation> choices = new ArrayList<>();
                JsonArray jsonChoices = jsonResponse.get("choices").getAsJsonArray();
                for (JsonElement jsonChoice : jsonChoices) {
                    JsonObject jsonObject = jsonChoice.getAsJsonObject();
                    choices.add(
                            TextVariation.builder()
                                    .index(jsonObject.get("index").getAsInt())
                                    .finishReason(jsonObject.get("finish_reason").getAsString())
                                    .logProbabilities(null)
                                    .content(jsonObject.get("text").getAsString())
                                    .build()
                    );
                }
                JsonObject jsonUsage = jsonResponse.get("usage").getAsJsonObject();
                ResponseUsage usage = ResponseUsage.builder()
                        .promptTokens(jsonUsage.get("prompt_tokens").getAsInt())
                        .completionTokens(jsonUsage.get("completion_tokens").getAsInt())
                        .totalTokens(jsonUsage.get("total_tokens").getAsInt())
                        .build();
                return CompletionResponse.builder()
                        .id(jsonResponse.get("id").getAsString())
                        .object(jsonResponse.get("object").getAsString())
                        .created(jsonResponse.get("created").getAsLong())
                        .model(CompletionModel.of(jsonResponse.get("model").getAsString()).get())
                        .textVariations(choices)
                        .usage(usage)
                        .user(jsonResponse.has("user") ? jsonResponse.get("user").getAsString() : null)
                        .build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, executorService).thenAcceptAsync(callback);
    }
}
