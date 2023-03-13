package me.drkapdor.openai4j.images.request;

public enum ResponseFormat {

    URL("url"),
    BASE64("b64_json");

    private final String value;

    ResponseFormat(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
