package me.drkapdor.openai4j.completions.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseUsage {

    /** Длина непосредственно запроса */
    private int promptTokens;
    /** Длина ответа, не включая символы запроса*/
    private int completionTokens;
    /** Общая длина ответа */
    private int totalTokens;

}
