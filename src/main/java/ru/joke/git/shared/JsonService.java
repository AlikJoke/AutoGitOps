package ru.joke.git.shared;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JsonService {

    private final Gson gson =
            new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DOTS)
                .disableJdkUnsafe()
                .setPrettyPrinting()
            .create();

    public String serialize(final Object obj) {
        return gson.toJson(obj);
    }

    public <T> T deserialize(final String json, final Class<T> tokenType) {
        return gson.fromJson(json, tokenType);
    }
}
