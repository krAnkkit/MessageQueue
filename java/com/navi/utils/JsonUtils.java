package com.navi.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtils {
    private JsonUtils() {}

    public static Gson gson = new GsonBuilder().create();
}
