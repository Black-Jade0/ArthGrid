package com.Prakhar.Auth_Service.Config;

public class PemUtils {


    public static String format(String value) {
        if (value == null) return null;
        return value.replace("\\n", "\n").trim();
    }
}
