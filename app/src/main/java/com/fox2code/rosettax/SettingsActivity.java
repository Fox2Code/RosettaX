package com.fox2code.rosettax;

import android.location.Location;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import java.util.HashSet;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            final HashSet<String> supportedLocales = new HashSet<>();
            supportedLocales.add("cs");
            supportedLocales.add("de");
            supportedLocales.add("el");
            supportedLocales.add("es-rMX");
            supportedLocales.add("et");
            supportedLocales.add("fr");
            supportedLocales.add("id");
            supportedLocales.add("it");
            supportedLocales.add("ja");
            supportedLocales.add("nb-rNO");
            supportedLocales.add("pl");
            supportedLocales.add("pt-rBR");
            supportedLocales.add("ro");
            supportedLocales.add("ru");
            supportedLocales.add("sk");
            supportedLocales.add("tr");
            supportedLocales.add("vi");
            supportedLocales.add("zh-rCH");
            supportedLocales.add("zh-rTW");
            supportedLocales.add("en");
            findPreference("language").setOnPreferenceClickListener(preference -> {
                LanguageSwitcher ls = new LanguageSwitcher(requireActivity());
                ls.setSupportedStringLocales(supportedLocales);
                ls.showChangeLanguageDialog(requireActivity());
                return true;
            });
            Locale locale = requireActivity().getResources().getConfiguration().locale;
            findPreference("language").setSummary(locale.getDisplayName(Locale.US));
            Location location = new Location("");
            location.getProvider();
        }
    }
}