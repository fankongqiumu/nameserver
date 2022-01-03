package com.github.nameserver.util;

import com.github.nameserver.exceptioin.NotSupportException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.*;

/**
 * @author fankongqiumu
 * @description
 * @date 2021/12/21 20:05
 */
public class JsonUtils {

    private static final Set<String> BLANK_JSON;

    static {
        BLANK_JSON = new HashSet<>();
        BLANK_JSON.add("[]");
        BLANK_JSON.add("{}");
    }

    private JsonUtils(){
        throw NotSupportException.createInstanceNotSupportException();
    }

    private static final Gson GSON = new GsonBuilder().create();

    public static <T> String toJsonString(T t) {
        return GSON.toJson(t);
    }

    public static <T> T  parse(String jsonString, Class<T> classOft) {
        return GSON.fromJson(jsonString, classOft);
    }

    public static <T> List<T>  parseList(String jsonString, Class<T> classOft) {
        return GSON.fromJson(jsonString, new TypeToken<List<T>>(){}.getType());
    }

    public static <T> Set<T>  parseSet(String jsonString, Class<T> classOft) {
        return GSON.fromJson(jsonString, new TypeToken<Set<T>>(){}.getType());
    }

    public static <T> T[]  parseArray(String jsonString, Class<T> classOft) {
        return GSON.fromJson(jsonString, new TypeToken<T[]>(){}.getType());

    }

    public static <K,V> Map<K,V> parseMap(String jsonString, Class<K> classOfK,  Class<V> classOfV) {
        return GSON.fromJson(jsonString, new TypeToken<HashMap<K,V>>(){}.getType());
    }

    public static Map parseMap(String jsonString) {
        return GSON.fromJson(jsonString, new TypeToken<HashMap>(){}.getType());
    }


    public static boolean isBlank(String jsonString){
        return null == jsonString || jsonString.isEmpty() || BLANK_JSON.contains(jsonString);
    }
}
