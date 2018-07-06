package it.br.objective.jira.util;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Json {

    private static final Gson gson = new GsonBuilder()
            .create();

    public static <T> T decode(String text, TypeToken<T> typeRef) {
        return gson.fromJson(text, typeRef.getType());
    }

    public static Stream<JSONObject> stream(JSONArray array) {
        return IntStream.range(0, array.length())
                .mapToObj(i -> (JSONObject) array.opt(i));
    }
}
