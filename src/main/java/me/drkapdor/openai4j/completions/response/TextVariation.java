package me.drkapdor.openai4j.completions.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

@Builder
@Getter
public class TextVariation {

    /** Содержимое сгенерированного ответа */
    private String content;
    /** Индекс сгенерированного ответа*/
    private int index;
    /** Наиболее подходящие заведомо сгенерированные ответы*/
    private String logProbabilities;
    /** Причина обрыва ответа*/
    private String finishReason;

    public Optional<String> getFinishReason() {
        return Optional.ofNullable(finishReason);
    }

}
