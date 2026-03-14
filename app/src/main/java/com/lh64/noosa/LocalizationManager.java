
package com.lh64.noosa;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import android.content.res.AssetManager;

public class LocalizationManager {
    private static LocalizationManager instance;
    private Map<String, String> translations;

    private LocalizationManager() {
        translations = new HashMap<>();
    }

    public static synchronized LocalizationManager getInstance() {
        if (instance == null) {
            instance = new LocalizationManager();
        }
        return instance;
    }

    public void init(AssetManager assets, String language) {
        Properties properties = new Properties();
        try {
            InputStream inputStream = assets.open("i18n/" + language + ".properties");
            properties.load(inputStream);
            for (String key : properties.stringPropertyNames()) {
                translations.put(key, properties.getProperty(key));
            }
        } catch (IOException e) {
            // In case of error, just use the keys as values
        }
    }

    public String getString(String key) {
        if (translations.containsKey(key)) {
            return translations.get(key);
        }
        return key; // Return the key itself if no translation is found
    }
}
