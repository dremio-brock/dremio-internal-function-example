package com.dremio.udf;

import java.util.concurrent.ConcurrentHashMap;

public class VaultCacheManager {
    private static final ConcurrentHashMap<String, String> CACHE = new ConcurrentHashMap<>();

    public static String getFromCache(String key) {
        return CACHE.get(key);
    }

    public static void putInCache(String key, String value) {
        CACHE.put(key, value);
    }

    public static void invalidateCache(String key) {
        CACHE.remove(key);
    }
}
