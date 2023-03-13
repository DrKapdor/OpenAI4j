package response;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import me.drkapdor.openai4j.common.IOpenAiResponse;

import java.util.Collection;
import java.util.Optional;

@Builder
@Getter
public class ImageResponse implements IOpenAiResponse {

    /** Дата генерации в формате Unix Time */
    private long creationDate;
    /** Список сгенерированных изображений */
    @Singular
    private Collection<GeneratedImage> images;
    /** Уникальный идентификатор инициатора запроса (опционально) */
    private String user;

    public Optional<String > getUser() {
        return Optional.ofNullable(user);
    }
}
