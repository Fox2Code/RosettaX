package com.fox2code.rosettax;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;

/**
 * This class is the application door to this Library. It handles the ongoing and outgoing requests,
 * initializations, preferences, ..
 * I think that there's no need for logging here because other classes already handle logs for these
 * actions based on their returned results.
 *
 * Created by ahmedjazzar on 1/16/16.
 */
public class LanguageSwitcher {
    private final Context mContext;
    private final String TAG = this.getClass().getSimpleName();

    /**
     * A constructor that accepts context and sets the base and first launch locales to en_US
     * @param application the application instance
     */
    public LanguageSwitcher(@NonNull Application application) {
        this(application, application.getResources().getConfiguration().locale, Locale.US);
    }

    /**
     * A constructor that accepts context and sets the base and first launch locales to en_US
     * @param context the context of the dealer
     */
    public LanguageSwitcher(@NonNull Context context) {
        this(context, Locale.US);
    }

    /**
     * A constructor that accepts context and sets the base and first launch locales to
     * firstLaunchLocale.
     *
     * NOTE: Please do not use unless:
     *  1. You wanna set your locales by calling {@link LanguageSwitcher#setSupportedLocales}
     *  2. You know for sure that the preferred locale is as same as your base locale
     *
     * @param context the context of the dealer
     * @param firstLaunchLocale the locale that owner wanna use at its first launch
     */
    public LanguageSwitcher(@NonNull Context context, Locale firstLaunchLocale) {
        this(context, firstLaunchLocale, firstLaunchLocale);
    }

    /**
     * This is supposed to be more specific; It has three parameters cover all owner needs
     * @param context the context of the dealer
     * @param firstLaunchLocale the locale that owner wanna use at its first launch
     * @param baseLocale the locale that used in the main xml strings file (most likely 'en')
     */
    public LanguageSwitcher(@NonNull Context context, Locale firstLaunchLocale, Locale baseLocale) {
        LocalesPreferenceManager preferenceManager = LocalesUtils.getLocalesPreferenceManager();
        this.mContext = context.getApplicationContext();


        if (preferenceManager == null) {
            preferenceManager = new LocalesPreferenceManager(
                    this.mContext, firstLaunchLocale, baseLocale);
            ((Application) this.mContext.getApplicationContext())
                    .registerActivityLifecycleCallbacks(preferenceManager);

            // initializing Locales utils needed objects (detector, preferences)
            LocalesUtils.setDetector(new LocalesDetector(this.mContext));
            LocalesUtils.setLocalesPreferenceManager(preferenceManager);
        }

        // Setting app locale to match the user preferred one
        LocalesUtils.setAppLocale(mContext, preferenceManager
                .getPreferredLocale(LocalesPreferenceManager.USER_PREFERRED_LOCALE));
    }

    /**
     * Responsible for displaying Change dialog fragment
     */
    public void showChangeLanguageDialog(FragmentActivity activity)  {
        showChangeLanguageDialogImpl(activity, TAG);
    }

    /**
     * Responsible for displaying Change dialog fragment
     */
    public static void showChangeLanguageDialogImpl(FragmentActivity activity, String tag)  {
        new LanguagesListDialogFragment().show(activity.getSupportFragmentManager(), tag);
    }

    /**
     *
     * @return the application supported locales
     */
    @NonNull
    public HashSet<Locale> getLocales()   {
        return LocalesUtils.getLocales();
    }

    /**
     * Sets the app locales from a string Set
     * @param sLocales supported locales in a String form
     */
    public void setSupportedStringLocales(String... sLocales)    {
        this.setSupportedStringLocales(Arrays.asList(sLocales));
    }

    /**
     * Sets the app locales from a string Set
     * @param sLocales supported locales in a String form
     */
    public void setSupportedStringLocales(Collection<String> sLocales)    {
        LinkedHashSet<Locale> locales = new LinkedHashSet<>();
        for (String sLocale: sLocales) {
            locales.add(LocalesUtils.parseLocale(sLocale));
        }
        this.setSupportedLocales(locales);
    }

    /**
     * set supported locales from the given Set
     * @param locales supported locales
     */
    public void setSupportedLocales(Locale... locales)    {
        this.setSupportedLocales(Arrays.asList(locales));
    }

    /**
     * set supported locales from the given Set
     * @param locales supported locales
     */
    public void setSupportedLocales(Collection<Locale> locales)    {
        LocalesUtils.setSupportedLocales(locales);
    }

    /**
     * Sets the supported locales after fetching there availability using fetchAvailableLocales
     * method
     * @param stringId the string that this library gonna use to detect current app available
     *                 locales
     */
    public void setSupportedLocales(@StringRes int stringId)    {
        this.setSupportedLocales(this.fetchAvailableLocales(stringId));
    }

    /**
     * Fetching the application available locales inside the resources folder dynamically
     * @param stringId the string that this library gonna use to detect current app available
     *                 locales
     * @return a set of detected application locales
     */
    public HashSet<Locale> fetchAvailableLocales(@StringRes int stringId) {
        return LocalesUtils.fetchAvailableLocales(stringId);
    }

    /**
     * Setting the application locale manually
     * @param newLocale the locale in a string format
     * @param activity the current activity in order to refresh the app
     *
     * @return true if the operation succeed, false otherwise
     */
    public boolean setLocale(String newLocale, Activity activity)   {
        return setLocale(new Locale(newLocale), activity);
    }

    /**
     * Setting the application locale manually
     * @param newLocale the desired locale
     * @param activity the current activity in order to refresh the app
     *
     * @return true if the operation succeed, false otherwise
     */
    public boolean setLocale(Locale newLocale, Activity activity)  {
        return LocalesUtils.setLocale(newLocale, activity);
    }

    /**
     * @return the first launch locale
     */
    @NonNull
    public Locale getLaunchLocale()  {
        return LocalesUtils.getLaunchLocale();
    }

    /**
     * @return the current locale
     */
    @NonNull
    public Locale getCurrentLocale()  {
        return LocalesUtils.getCurrentLocale(this.mContext);
    }

    /**
     * Return to the first launch locale
     * @param activity the current activity in order to refresh the app
     *
     * @return true if the operation succeed, false otherwise
     */
    public boolean switchToLaunch(@NonNull Activity activity)  {
        return setLocale(getLaunchLocale(), activity);
    }
}
