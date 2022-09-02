package com.fox2code.rosettax;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * This class detects the application available locales inside the resources based on a string id,
 * it's not so accurate and expects another methodologies. Next release may hold a better algorithms
 * for detecting strings' languages and availability inside apps.
 *
 * Created by ahmedjazzar on 1/16/16.
 */
final class LocalesDetector {
    private static final String TAG = LocalesDetector.class.getName();

    private final Context mContext;
    private final Logger mLogger;

    LocalesDetector(Context context)    {
        this.mContext = context;
        this.mLogger = new Logger(TAG);
    }

    /**
     * this method takes an experimental string id to see if it's exists in other available
     * locales inside the app than default locale.
     * NOTE: Even if you have a folder named values-ar it doesn't mean you have any resources
     *      there
     *
     * @param stringId experimental string id to discover locales
     * @return the discovered locales
     */
    HashSet<Locale> fetchAvailableLocales(@StringRes int stringId) {

        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        Configuration conf = mContext.getResources().getConfiguration();
        Locale originalLocale = conf.locale;
        Locale baseLocale = LocalesUtils.getBaseLocale();
        conf.locale = baseLocale;

        ArrayList<String> references = new ArrayList<>();
        references.add(new Resources(mContext.getAssets(), dm, conf).getString(stringId));

        HashSet<Locale> result = new HashSet<>();
        result.add(baseLocale);

        for(String loc : mContext.getAssets().getLocales()) {
            if(loc.isEmpty()){
                continue;
            }

            Locale l;
            boolean referencesUpdateLock = false;

            l = Locale.forLanguageTag(loc);

            conf.locale = l;

            //TODO: put it in a method
            String tmpString = new Resources(mContext.getAssets(), dm, conf).getString(stringId);
            for (String reference: references)  {
                if(reference.equals(tmpString)){
                    // TODO: check its original locale
                    referencesUpdateLock = true;
                    break;
                }
            }

            if(!referencesUpdateLock)   {
                result.add(l);
                references.add(tmpString);
            }
        }

        conf.locale = originalLocale; // to restore our guy initial state
        return result;
    }

    /**
     * TODO: return the selected one instead
     * @return application current locale
     */
    Locale getCurrentLocale()   {
        return mContext.getResources().getConfiguration().locale;
    }

    /**
     * TODO: what if a user didn't provide a closer email at all?
     *
     * This method should provide a locale that is close to the given one in the parameter, it's
     * currently checking the language only if in case the detector detects the string in other
     * language.
     *
     * @param locale mostly the locale that's not detected or provided
     * @return the index of the most close locale to the given locale. -1 if not detected
     */
    int detectMostClosestLocale(Locale locale)   {

        mLogger.debug("Start detecting a close locale to: ");

        int index = -1;
        int precision = 0;
        Locale localeClosest = null;
        int indexLocaleClosest = -1;
        String countryFallback = LocalesUtils.getCountryFallback(locale);
        for (Locale loc: LocalesUtils.getLocales()) {
            index++;
            if (loc.equals(locale)) {
                localeClosest = loc;
                indexLocaleClosest = index;
                precision = 3;
                break; // Break
            }
            if (loc.getDisplayLanguage().equals(locale.getDisplayLanguage()) &&
                    (countryFallback != null && countryFallback.equals(locale.getCountry()))) {
                localeClosest = loc;
                indexLocaleClosest = index;
                precision = 2;
                continue;
            }
            if (precision >= 2) continue;
            if (loc.getDisplayLanguage().equals(locale.getDisplayLanguage())) {
                localeClosest = loc;
                indexLocaleClosest = index;
                precision = 1;
                continue;
            }
            if (precision >= 1) continue;
            if (locale.getLanguage().equals(loc.getLanguage()) &&
                    (indexLocaleClosest == -1 || (countryFallback != null &&
                            countryFallback.equals(locale.getCountry())))) {
                localeClosest = loc;
                indexLocaleClosest = index;
            }
        }

        if (localeClosest != null && indexLocaleClosest != -1) {
            if (precision == 3) {
                mLogger.info("The locale: '" + localeClosest +
                        "' has been detected as the same locale to: '" + locale + "'");
            } else {
                mLogger.info("The locale: '" + localeClosest +
                        "' has been detected as a closer locale to: '" + locale + "'");
            }
            return indexLocaleClosest;
        } else {
            mLogger.debug("No closer locales found.");
            return -1;
        }
    }

    /**
     * This method validate locales by checking if they are available of they contain wrong letter
     * case and adding the valid ones in a clean set.
     * @param locales to be checked
     * @return valid locales
     */
    LinkedHashSet<Locale> validateLocales(Collection<Locale> locales) {

        mLogger.debug("Validating given locales..");

        if (!(locales instanceof Set)) {
            locales = new LinkedHashSet<>(locales);
        }

        for (Locale l:LocalesUtils.getPseudoLocales()) {
            if(locales.remove(l)) {
                mLogger.info("Pseudo locale '" + l + "' has been removed.");
            }
        }

        LinkedHashSet<Locale> cleanLocales = new LinkedHashSet<>();
        Locale[] androidLocales = Locale.getAvailableLocales();
        for (Locale locale: locales) {
            if (Arrays.asList(androidLocales).contains(locale)) {
                cleanLocales.add(locale);
            } else {
                // Workaround for chinese not being correctly detected
                switch ("" + locale) {
                    case "zh":
                        cleanLocales.add(Locale.CHINESE);
                        continue;
                    case "zh_CH":
                        cleanLocales.add(Locale.SIMPLIFIED_CHINESE);
                        continue;
                    case "zh_TW":
                        cleanLocales.add(Locale.TRADITIONAL_CHINESE);
                        continue;
                }
                mLogger.error("Invalid passed locale: " + locale);
                mLogger.warn("Invalid specified locale: '" + locale + "', has been discarded");
            }
        }
        mLogger.debug("passing validated locales.");
        return cleanLocales;
    }
}
