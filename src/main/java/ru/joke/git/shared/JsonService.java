package ru.joke.git.shared;

import com.google.gson.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

public final class JsonService {

    private final Gson gson =
            new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DOTS)
                    .disableJdkUnsafe()
                    .setPrettyPrinting()
                    .addSerializationExclusionStrategy(new ExclusionStrategy() {
                        @Override
                        public boolean shouldSkipField(FieldAttributes f) {
                            return false;
                        }

                        @Override
                        public boolean shouldSkipClass(Class<?> clazz) {
                            return clazz == RevCommit.class || ObjectId.class.isAssignableFrom(clazz);
                        }
                    })
            .create();

    public String serialize(final Object obj) {
        return gson.toJson(obj);
    }

    public <T> T deserialize(final String json, final Class<T> tokenType) {
        return gson.fromJson(json, tokenType);
    }
}
