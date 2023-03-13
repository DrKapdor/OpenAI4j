package me.drkapdor.openai4j.images.request;

public enum ImageSize {

    SMALL("256x256"),
    MEDIUM("512x512"),
    LARGE("1024x1024");

    private final String value;

    ImageSize(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
