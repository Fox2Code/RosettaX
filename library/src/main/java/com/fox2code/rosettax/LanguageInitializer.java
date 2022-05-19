package com.fox2code.rosettax;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class LanguageInitializer implements Initializer<Boolean> {
    @NonNull
    @Override
    public Boolean create(@NonNull Context context) {
        // Initialize only if not already initialized
        if (LocalesUtils.getLocalesPreferenceManager() == null) {
            new LanguageSwitcher(context, context.getApplicationContext()
                    .getResources().getConfiguration().locale, Locale.US);
        }
        return Boolean.TRUE;
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }
}
