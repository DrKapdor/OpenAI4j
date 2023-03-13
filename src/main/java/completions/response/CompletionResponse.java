package completions.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import me.drkapdor.openai4j.common.IOpenAiResponse;
import me.drkapdor.openai4j.completions.CompletionModel;

import java.util.List;
import java.util.Optional;

@Builder
@Getter
public class CompletionResponse implements IOpenAiResponse {

    /** Идентификатор возвращаемого ответа */
    private String id;
    /** Тип объекта */
    private String object;
    /** Дата генерации в формате Unix Time */
    private long created;
    /** Текстовая модель, в соответствии с которой генерировались ответы */
    private CompletionModel model;
    /** Список сгенерированных вариаций ответов */
    @Singular
    private List<TextVariation> textVariations;
    /** Информация об использованных символах*/
    private ResponseUsage usage;
    /** Уникальный идентификатор инициатора запроса (опционально) */
    private String user;

    public Optional<String > getUser() {
        return Optional.ofNullable(user);
    }
}
