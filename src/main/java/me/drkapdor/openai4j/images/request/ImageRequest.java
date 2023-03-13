package me.drkapdor.openai4j.images.request;

import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Getter;
import me.drkapdor.openai4j.common.IOpenAiRequest;

@Builder
@Getter
public class ImageRequest implements IOpenAiRequest {

    /** Описание, согласно которому будет генерироваться изображение */
    private String description;
    /** Количество генерируемых изображений */
    @Builder.Default
    private int imagesCount = 1;
    /** Формат размера изображения */
    @Builder.Default
    private ImageSize size = ImageSize.SMALL;
    /** Формат, в котором будет получено изображение */
    @Builder.Default
    private ResponseFormat responseFormat = ResponseFormat.URL;
    /** Уникальный идентификатор инициатора запроса (опционально) */
    private String user;

    public JsonObject toJson() {
        JsonObject request = new JsonObject();
        request.addProperty("prompt", description);
        request.addProperty("n", imagesCount);
        request.addProperty("size", size.toString());
        request.addProperty("response_format", responseFormat.toString());
        if (user != null) request.addProperty("user", user);
        return request;
    }

}
