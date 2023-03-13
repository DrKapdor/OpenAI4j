package me.drkapdor.openai4j;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import lombok.Builder;
import me.drkapdor.openai4j.common.IOpenAiRequest;
import me.drkapdor.openai4j.completions.CompletionModel;
import me.drkapdor.openai4j.completions.request.CompletionRequest;
import me.drkapdor.openai4j.completions.response.CompletionResponse;
import me.drkapdor.openai4j.completions.response.ResponseUsage;
import me.drkapdor.openai4j.completions.response.TextVariation;
import me.drkapdor.openai4j.images.request.ImageRequest;
import me.drkapdor.openai4j.images.response.GeneratedImage;
import me.drkapdor.openai4j.images.response.ImageResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Клиент для взаимодействия с сервисами OpenAI
 * @author DrKapdor
 */
@Builder
public class OpenAiClient {

    /** URL сервиса генерации текстовых сообщений OpenAI */
    private static final String COMPLETIONS_URL = "https://api.openai.com/v1/completions";
    /** URL сервиса генерации изображений OpenAI */
    private static final String IMAGES_URL = "https://api.openai.com/v1/images/generations";

    /** Токен доступа к сервисам OpenAI */
    private String accessToken;
    /** Служба потоковых исполнителей */
    private ExecutorService executorService;
    /** Время ожидания подключения к сервисам OpenAI */
    @Builder.Default
    private int connectionTimeout = 30000;
    /** Время ожидания ответа от сервисов OpenAI*/
    @Builder.Default
    private int requestTimeout = 30000;
    /** Время ожидания чтения сокета */
    @Builder.Default
    private int socketTimeout = 30000;

    /**
     * Формулирует набор ответов в соответствии с поставленным вопросом
     * @param request Запрос, содержащий параметры желаемого ответа
     * @return Ответ, содержащий информацию о сгенерированных формулировках
     */

    public CompletableFuture<CompletionResponse> generateCompletions(CompletionRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<JsonObject> responseOptional = prepareJsonResponse(request, COMPLETIONS_URL);
            if (responseOptional.isPresent()) {
                JsonObject jsonResponse = responseOptional.get();
                List<TextVariation> choices = new ArrayList<>();
                JsonArray jsonChoices = jsonResponse.get("choices").getAsJsonArray();
                for (JsonElement jsonChoice : jsonChoices) {
                    JsonObject jsonObject = jsonChoice.getAsJsonObject();
                    choices.add(TextVariation.builder()
                            .index(jsonObject.get("index").getAsInt())
                            .finishReason(jsonObject.get("finish_reason").getAsString())
                            .logProbabilities(null)
                            .content(jsonObject.get("text").getAsString())
                            .build());
                }
                JsonObject jsonUsage = jsonResponse.get("usage").getAsJsonObject();
                ResponseUsage usage = ResponseUsage.builder()
                        .promptTokens(jsonUsage.get("prompt_tokens").getAsInt())
                        .completionTokens(jsonUsage.get("completion_tokens").getAsInt())
                        .totalTokens(jsonUsage.get("total_tokens").getAsInt())
                        .build();
                return CompletionResponse.builder()
                        .success(true)
                        .id(jsonResponse.get("id").getAsString())
                        .object(jsonResponse.get("object").getAsString())
                        .created(jsonResponse.get("created").getAsLong())
                        .model(CompletionModel.of(jsonResponse.get("model").getAsString()).get())
                        .textVariations(choices)
                        .usage(usage)
                        .user(jsonResponse.has("user") ? jsonResponse.get("user").getAsString() : null)
                        .build();
            } else return CompletionResponse.builder()
                    .success(false)
                    .user(request.getUser())
                    .model(request.getModel())
                    .build();
        }, executorService);
    }

    /**
     * Генерирует набор изображений в соответствии с указанным описанием
     * @param request Запрос, содержащий параметры желаемого изображения
     * @return Ответ, содержащий информацию о сгенерированном изображении
     */

    public CompletableFuture<ImageResponse> generateImages(ImageRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<JsonObject> responseOptional = prepareJsonResponse(request, IMAGES_URL);
            if (responseOptional.isPresent()) {
                JsonObject jsonResponse = responseOptional.get();
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
            } else return ImageResponse.builder()
                    .user(request.getUser())
                    .creationDate(System.currentTimeMillis())
                    .build();
        }, executorService);
    }

    /**
     * Подготавливает ответ в формате JSON от сервиса OpenAI
     * @param request Запрос к сервису OpenAI
     * @param url URL сервиса OpenAI
     * @return Подготовленный ответ в офрмате JSON
     */
    private Optional<JsonObject> prepareJsonResponse(IOpenAiRequest request, String url) {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(RequestConfig.custom()
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectionTimeout)
                .setConnectionRequestTimeout(requestTimeout)
                .build());
        httpPost.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        httpPost.addHeader("Authorization", "Bearer " + accessToken);
        httpPost.setEntity(new StringEntity(request.toJson().toString(), ContentType.APPLICATION_JSON));
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                InputStreamReader inputStreamReader = new InputStreamReader(httpResponse.getEntity().getContent());
                JsonObject jsonResponse = new JsonParser().parse(new JsonReader(inputStreamReader)).getAsJsonObject();
                httpClient.close();
                return Optional.of(jsonResponse);
            } else {
                httpClient.close();
                return Optional.empty();
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}