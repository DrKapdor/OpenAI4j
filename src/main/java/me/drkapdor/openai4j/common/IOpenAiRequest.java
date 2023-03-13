package me.drkapdor.openai4j.common;

import com.google.gson.JsonObject;

public interface IOpenAiRequest {

    JsonObject toJson();

}
