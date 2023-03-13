package me.drkapdor.openai4j.completions.request;

import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.Getter;
import me.drkapdor.openai4j.common.IOpenAiRequest;
import me.drkapdor.openai4j.completions.CompletionModel;

@Builder
@Getter
public class CompletionRequest implements IOpenAiRequest {

    /** Текстовая модель, в соответствии с которой будут генерироваться ответы */
    @Builder.Default
    private CompletionModel model = CompletionModel.DAVINCI;
    /** Текст запроса */
    private String question;
    /** Креативность (мягкость) получаемой формулировки от 0.0 до 2.0 */
    @Builder.Default
    private float creativity = 0;
    /** Длина получаемого ответа (в символах) */
    @Builder.Default
    private int responseLength = 128;
    /** Количество получаемых формулировок */
    @Builder.Default
    private int variations = 1;
    /** Уникальный идентификатор инициатора запроса (опционально) */
    private String user;

    @Override
    public JsonObject toJson() {
        JsonObject parameters = new JsonObject();
        parameters.addProperty("model", model.getId());
        parameters.addProperty("prompt", question);
        parameters.addProperty("max_tokens", responseLength);
        parameters.addProperty("temperature", creativity);
        parameters.addProperty("n", variations);
        if (user != null) parameters.addProperty("user", user);
        return parameters;
    }

}
