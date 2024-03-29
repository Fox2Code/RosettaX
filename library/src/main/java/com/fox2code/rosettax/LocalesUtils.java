package com.fox2code.rosettax;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentActivity;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

/**
 * This class is a helper class that connects all library classes activities together and make it
 * easier for every class in the library to use and look at the shared info without a need to
 * initialize a new object from the desired class
 *
 * Created by ahmedjazzar on 1/19/16.
 */
final class LocalesUtils {

    @SuppressLint("StaticFieldLeak")
    private static LocalesDetector sDetector;
    private static LocalesPreferenceManager sLocalesPreferenceManager;
    private static LinkedHashSet<Locale> sLocales =
            new LinkedHashSet<>(Collections.singleton(Locale.US));
    private static Locale[] sLocalesArray;
    private static final HashMap<String, String> sCountryFallback = new HashMap<>();
    private static final Locale[] PSEUDO_LOCALES = {
            new Locale("en", "XA"),
            new Locale("ar", "XB")
    };
    private static final String TAG = LocalesDetector.class.getName();
    private static final Logger sLogger = new Logger(TAG);

    static {
        // en (English) and zh (Chinese) is handled by code instead
        sCountryFallback.put("fr", "FR");
        sCountryFallback.put("de", "DE");
        sCountryFallback.put("it", "IT");
        sCountryFallback.put("ja", "JP");
        sCountryFallback.put("ko", "KR");
    }

    /**
     * @param translation translation id to use
     */
    static int getIdOfTranslation(@NonNull String translation) {
        try {
            return android.R.string.class.getDeclaredField(translation).getInt(null);
        } catch (Throwable ignored) {
            sLogger.debug("Translation \"" + translation + "\" doesn't exists on this system!");
        }
        try {
            switch (translation) {
                case "ok":
                    return R.string.rosetta_ok;
                case "cancel":
                    return R.string.rosetta_cancel;
                case "language":
                    return R.string.rosetta_language;
                default:
                    return R.string.class.getDeclaredField(
                            "rosetta_" + translation).getInt(null);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to get: " + translation, e);
        }
    }

    /**
     * @param detector just a setter because I don't want to declare any constructors in this class
     */
    static void setDetector(@NonNull LocalesDetector detector) {
        LocalesUtils.sDetector = detector;
    }

    /**
     * @param localesPreferenceManager just a setter because I don't want to declare any
     *                                 constructors in this class
     */
    static void setLocalesPreferenceManager(
            @NonNull LocalesPreferenceManager localesPreferenceManager) {
        LocalesUtils.sLocalesPreferenceManager = localesPreferenceManager;
    }

    /**
     * @return the localesPreferenceManager instance
     */
    public static LocalesPreferenceManager getLocalesPreferenceManager() {
        return LocalesUtils.sLocalesPreferenceManager;
    }

    /**
     * @param stringId a string to start discovering sLocales in
     * @return a HashSet of discovered sLocales
     */
    static HashSet<Locale> fetchAvailableLocales(@StringRes int stringId) {
        return sDetector.fetchAvailableLocales(stringId);
    }

    /**
     * @param localesSet sLocales  user wanna use
     */
    static void setSupportedLocales(Collection<Locale> localesSet) {
        if (LocalesUtils.sLocalesArray != null) {
            sLogger.warn("Setting supported locales twice is not supported!");
        }
        LocalesUtils.sLocales = sDetector.validateLocales(localesSet);
        LocalesUtils.sLocalesArray = LocalesUtils.sLocales.toArray(new Locale[0]);
        sLogger.debug("Locales have been changed");
    }

    /**
     * @return a HashSet of the available sLocales discovered in the application
     */
    static HashSet<Locale> getLocales() {
        return LocalesUtils.sLocales;
    }

    /**
     * @return a list of locales for displaying on the layout purposes
     */
    static ArrayList<String> getLocalesWithDisplayName() {
        ArrayList<String> stringLocales = new ArrayList<>();

        for (Locale loc : LocalesUtils.getLocales()) {
            String langDisplay = loc.getDisplayName(loc);
            sLogger.debug(loc + ": " + langDisplay);
            stringLocales.add(langDisplay.substring(0, 1).toUpperCase() + langDisplay.substring(1).toLowerCase());
        }
        return stringLocales;
    }

    /**
     * @return the index of the current app locale
     */
    static int getCurrentLocaleIndex() {
        Locale locale = LocalesUtils.getCurrentLocale();
        int index = -1;
        int indexSec = -1;
        int itr = 0;

        String countryFallback = sCountryFallback.get(locale.getLanguage());
        for (Locale l : sLocales) {
            if (locale.equals(l)) {
                index = itr;
                break;
            }
            if (locale.getLanguage().equals(l.getLanguage()) &&
                    (indexSec == -1 || (countryFallback != null &&
                            countryFallback.equals(locale.getCountry())))) {
                indexSec = itr;
            }
            itr++;
        }

        if (index == -1) {
            // change the index to the most closer available locale
            index = indexSec;
        }

        if (index == -1) {
            sLogger.warn("Current device locale '" + locale +
                    "' does not appear in your given supported locales");

            index = sDetector.detectMostClosestLocale(locale);
            if (index == -1) {
                index = 0;
                sLogger.warn("Current locale index changed to 0 as the current locale '" +
                        locale +
                        "' not supported."
                );
            }
        }

        return index;
    }

    /**
     * @return pseudo locales list
     * @see <a href="http://en.wikipedia.org/wiki/Pseudolocalization">Pseudolocalization</a> for
     * more information about pseudo localization
     */
    static List<Locale> getPseudoLocales() {
        return Arrays.asList(LocalesUtils.PSEUDO_LOCALES);
    }

    /**
     * @return the locale at the given index
     */
    static Locale getLocaleFromIndex(int index) {
        if (sLocalesArray == null) return Locale.US;
        return sLocalesArray[index];
    }

    /**
     * @return the index of the given locale
     */
    static int getIndexOfLocale(Locale locale) {
        if (sLocalesArray == null || locale == null) {
            return Locale.US.equals(locale) ? 0 : -1;
        }
        for (int i = 0; i < sLocalesArray.length; i++) {
            if (sLocalesArray[i].equals(locale))
                return i;
        }
        return -1;
    }

    /**
     * @param context the application context
     * @param index   the selected locale position
     * @return true if the application locale changed
     */
    static boolean setAppLocale(Context context, int index) {
        return setAppLocale(context, getLocaleFromIndex(index));
    }

    /**
     * @return true if the application locale changed
     */
    static boolean setAppLocale(Context context, Locale newLocale) {
        Resources resources = context.getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();

        Locale oldLocale = new Locale(configuration.locale.getLanguage(), configuration.locale.getCountry());
        configuration.locale = newLocale;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String language = newLocale.getLanguage();
            LocaleList localeList;
            if (language.equals("zh")) {
                if (Locale.SIMPLIFIED_CHINESE.equals(newLocale)) {
                    localeList = new LocaleList(newLocale,
                            Locale.TRADITIONAL_CHINESE, Locale.US);
                } else {
                    localeList = new LocaleList(newLocale,
                            Locale.SIMPLIFIED_CHINESE, Locale.US);
                }
            } else {
                String countryFallback = sCountryFallback.get(language);
                if (countryFallback != null && !countryFallback.equals(newLocale.getCountry())) {
                    localeList = new LocaleList(newLocale,
                            new Locale(language, countryFallback), Locale.US);
                } else if (!Locale.US.equals(newLocale)) {
                    localeList = new LocaleList(newLocale, Locale.US);
                } else {
                    localeList = new LocaleList(Locale.US);
                }
            }
            configuration.setLocales(localeList);
        }
        // Sets the layout direction from the Locale
        sLogger.debug("Setting the layout direction");
        configuration.setLayoutDirection(newLocale);
        resources.updateConfiguration(configuration, displayMetrics);

        if (oldLocale.equals(newLocale)) {
            return false;
        }

        if (LocalesUtils.updatePreferredLocale(newLocale)) {
            sLogger.info("Locale preferences updated to: " + newLocale);
            Locale.setDefault(newLocale);
        } else {
            sLogger.error("Failed to update locale preferences.");
        }

        return true;
    }

    /**
     * @return application's base locale
     */
    static Locale getBaseLocale() {
        return LocalesUtils.sLocalesPreferenceManager.getPreferredLocale(LocalesPreferenceManager.BASE_LOCALE);
    }

    /**
     * @param stringId the target string
     * @return a localized string
     */
    static String getInSpecificLocale(FragmentActivity activity, Locale locale, int stringId) {

        Configuration conf = activity.getResources().getConfiguration();
        Locale old = conf.locale;

        conf.locale = locale;
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Resources resources = new Resources(activity.getAssets(), metrics, conf);
        conf.locale = old;

        return resources.getString(stringId);
    }

    /**
     * Refreshing the application so no weired results occurred after changing the locale.
     */
    static void refreshApplication(Activity activity) {
        // Add support for explicitly rosetta compatible activities
        if (activity instanceof LanguageActivity) {
            ((LanguageActivity) activity).refreshRosettaX();
            return;
        }
        Method method = null;
        try {
            method = activity.getClass().getMethod("refreshRosettaX");
        } catch (NoSuchMethodException ignored) {}
        if (method != null && !Modifier.isAbstract(method.getModifiers())) {
            try {
                method.invoke(activity);
            } catch (ReflectiveOperationException e) {
               throw new RuntimeException(e);
            }
        }

        Intent app = activity.getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(activity.getBaseContext().getPackageName());
        app.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Intent current = new Intent(activity, activity.getClass());
        sLogger.debug("Refreshing the application: " +
                activity.getBaseContext().getPackageName());

        sLogger.debug("Finishing current activity.");
        activity.finish();

        sLogger.debug("Start the application");
        activity.startActivity(app);
        activity.startActivity(current);

        sLogger.debug("Application refreshed");
    }

    /**
     * @return the first launch locale
     */
    static Locale getLaunchLocale() {
        return sLocalesPreferenceManager.getPreferredLocale(LocalesPreferenceManager.LAUNCH_LOCALE);
    }

    /**
     * Setting the application locale manually
     *
     * @param newLocale the desired locale
     * @param activity  the current activity in order to refresh the app
     * @return true if the operation succeed, false otherwise
     */
    static boolean setLocale(Locale newLocale, Activity activity) {
        if (newLocale == null || !getLocales().contains(newLocale)) {
            return false;
        }

        if (LocalesUtils.setAppLocale(activity.getApplicationContext(), newLocale)) {
            LocalesUtils.refreshApplication(activity);
            return true;
        }

        return false;
    }

    /**
     * @param context application base context
     * @return the current locale
     */
    public static Locale getCurrentLocale(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();

        return new Locale(configuration.locale.getLanguage(), configuration.locale.getCountry());
    }

    /**
     * @param locale the new preferred locale
     * @return true if the preferred locale updated
     */
    private static boolean updatePreferredLocale(Locale locale) {
        return LocalesUtils.sLocalesPreferenceManager
                .setPreferredLocale(LocalesPreferenceManager.USER_PREFERRED_LOCALE, locale);
    }

    /**
     * @return current application locale
     */
    private static Locale getCurrentLocale() {
        return sDetector.getCurrentLocale();
    }

    /**
     * @param locale the locale string
     * @return parsed local object
     */
    static Locale parseLocale(String locale) {
        int i = locale.indexOf('-');
        return i == -1 || i + 1 == locale.length() ? new Locale(locale) :
                new Locale(locale.substring(0, i), locale.substring(
                        i + (locale.charAt(i + 1) == 'r' ? 2 : 1)));
    }

    /**
     * @param locale the locale
     * @return the country fallback for the provided locale
     */
    static String getCountryFallback(Locale locale) {
        return sCountryFallback.get(locale.getLanguage());
    }
}