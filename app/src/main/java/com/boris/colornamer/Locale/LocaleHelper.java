package com.boris.colornamer.Locale;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

public class LocaleHelper {

    /**
     * Sets the language at runtime
     *
     * @param context  the current context to get the configuration
     * @param language the language to set ("en" for english, "fr" for french)
     * @return the new context configured with the given language
     */
    public static Context setLocale(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

}
