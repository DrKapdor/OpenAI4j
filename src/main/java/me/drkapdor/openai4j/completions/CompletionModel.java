package me.drkapdor.openai4j.completions;

import lombok.Getter;

import java.util.Optional;

@Getter
public enum CompletionModel {

    /** Наиболее совершенная текстовая модель */
    DAVINCI("text-davinci-003"),
    CURIE("text-curie-001"),
    BABBAGE("text-babbage-001"),
    ADA("text-ada-001");

    public static Optional<CompletionModel> of(String id) {
        for (CompletionModel model : values()) {
            if (model.getId().equals(id))
                return Optional.of(model);
        }
        return Optional.empty();
    }

    public static CompletionModel unsafe(String id) {
        for (CompletionModel model : values()) {
            if (model.getId().equals(id))
                return model;
        }
        return null;
    }

    private final String id;
    CompletionModel(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
