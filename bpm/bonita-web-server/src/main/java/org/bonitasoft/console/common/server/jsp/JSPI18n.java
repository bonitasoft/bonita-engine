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
package org.bonitasoft.console.common.server.jsp;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.console.common.server.utils.LocaleUtils;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n.LOCALE;

/**
 * Used in JSP. Do not delete.
 */
public class JSPI18n {

    private final LOCALE locale;
    private final JSPUtils jspUtils;

    public JSPI18n(JSPUtils jspUtils) {
        this.jspUtils = jspUtils;
        this.locale = loadLocale();

        // initialize I18n instance
        I18n.getInstance();
    }

    public String t_(String message) {
        return I18n.t_(message, locale);
    }

    public LOCALE getLocale() {
        return locale;
    }

    private LOCALE loadLocale() {
        String localeAsString = LocaleUtils.getUserLocaleAsString(jspUtils.getRequest());
        LOCALE locale = null;
        try {
            locale = LOCALE.valueOf(localeAsString);
        } catch (IllegalArgumentException e) {
            //this should not happen as the LOCALE enum is supposed to contain all locales
            LocaleUtils.logUnsupportedLocale(localeAsString, LocaleUtils.DEFAULT_LOCALE);
        }
        return locale;
    }
}
