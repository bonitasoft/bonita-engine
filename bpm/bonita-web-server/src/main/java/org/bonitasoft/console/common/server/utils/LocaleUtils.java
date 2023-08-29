/**
 * Copyright (C) 2022 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.console.common.server.utils;

import java.util.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anthony Birembaut
 */
public class LocaleUtils {

    public static final String LOCALE_PARAM = "locale";

    /**
     * user's locale URL parameter used by the login JSP
     */
    public static final String PORTAL_LOCALE_PARAM = "_l";

    public static final String DEFAULT_LOCALE = "en";

    public static final String LOCALE_COOKIE_NAME = "BOS_Locale";

    private static final String DEFAULT_APPLICATION = "portal";

    private static final I18n i18n = I18n.getInstance();

    private static final Map<String, String> AVAILABLE_LOCALES = i18n.getAvailableLocalesFor(DEFAULT_APPLICATION);

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LocaleUtils.class.getName());

    /**
     * Return the user locale as set in the the request. If the locale code is invalid, returns the default locale (en).
     * If the locale is not passed in the request try to get it from the BOS_Locale cookie.
     * If the cookie is not set returns the Browser locale
     * If the browser locale is not set returns the default locale (en).
     * This method should be used sparingly for performances reasons (gather in one call when possible)
     *
     * @param request
     *        the HTTP servlet request
     * @return the user locale
     */
    public static String getUserLocaleAsString(final HttpServletRequest request) {
        String locale = getLocaleFromRequestURL(request);
        if (locale == null) {
            locale = getStandardizedLocaleFromCookie(request);
        }
        if (locale == null) {
            String browserLocale = getLocaleFromBrowser(request);
            locale = browserLocale != null ? browserLocale : DEFAULT_LOCALE;
        }
        return locale;
    }

    public static void logUnsupportedLocale(String unsupportedLocale, String usedLocale) {
        if (!unsupportedLocale.equals(usedLocale) && LOGGER.isDebugEnabled()) {
            LOGGER.debug("Locale \"" + unsupportedLocale + "\" is not part of the locales available. Using locale \""
                    + usedLocale + "\"");
        }
    }

    public static boolean isLocaleSupportedInPortal(String locale) {
        return new ArrayList<>(AVAILABLE_LOCALES.keySet()).contains(locale);
    }

    public static boolean canLocaleBeReducedToSupportedLocale(String locale, String supportedLocale) {
        if (locale.indexOf("-") > 0) {
            return locale.substring(0, locale.indexOf("-")).equals(supportedLocale);
        } else if (locale.indexOf("_") > 0) {
            return locale.substring(0, locale.indexOf("_")).equals(supportedLocale);
        }
        return false;
    }

    private static String standardizeLocale(String locale) {
        if (isLocaleSupportedInPortal(locale)) {
            return locale;
        } else {
            List<String> supportedLocales = new ArrayList<>(AVAILABLE_LOCALES.keySet());
            for (String supportedLocale : supportedLocales) {
                if (canLocaleBeReducedToSupportedLocale(locale, supportedLocale)) {
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(
                                "Using available locale \"" + supportedLocale + "\" instead of \"" + locale + "\"");
                    }
                    return supportedLocale;
                }
            }
        }

        logUnsupportedLocale(locale, "en");
        return DEFAULT_LOCALE;
    }

    public static String getStandardizedLocaleFromCookie(final HttpServletRequest request) {
        String locale = getLocaleFromCookies(request);
        return locale != null ? standardizeLocale(locale) : null;
    }

    public static String getLocaleFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (final Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(LOCALE_COOKIE_NAME)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static String getLocaleFromBrowser(final HttpServletRequest request) {
        Locale browserLocale = request.getLocale();
        return browserLocale != null ? standardizeLocale(browserLocale.toString()) : null;
    }

    public static Locale getUserLocale(final HttpServletRequest request) {
        return org.apache.commons.lang3.LocaleUtils.toLocale(getUserLocaleAsString(request));
    }

    public static String getLocaleFromRequestURL(final HttpServletRequest request) {
        String localeAsString = request.getParameter(LOCALE_PARAM);
        if (localeAsString == null) {
            localeAsString = request.getParameter(PORTAL_LOCALE_PARAM);
        }
        if (localeAsString != null && localeAsString.length() > 0) {
            try {
                org.apache.commons.lang3.LocaleUtils.toLocale(localeAsString);
                localeAsString = standardizeLocale(localeAsString);
            } catch (Exception e) {
                logUnsupportedLocale(localeAsString, "en");
                localeAsString = DEFAULT_LOCALE;
            }
            return localeAsString;
        }
        return null;
    }

    public static void addOrReplaceLocaleCookieResponse(final HttpServletResponse response, final String locale) {
        String standardizedLocale = standardizeLocale(locale);

        Cookie localeCookie = new Cookie(LOCALE_COOKIE_NAME, standardizedLocale);
        localeCookie.setPath("/");
        response.addCookie(localeCookie);
    }
}
