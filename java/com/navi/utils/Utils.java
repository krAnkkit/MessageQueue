package com.navi.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.logging.Logger;

public class Utils {
    private Utils() {}

    public static Gson gson = new GsonBuilder().create();
    public static Logger logger = Logger.getLogger("com.navi");
}
