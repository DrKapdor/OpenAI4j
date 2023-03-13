package me.drkapdor.openai4j.images.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GeneratedImage {

    /** Информация об изображении */
    private final String content;

}
