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
package org.bonitasoft.web.rest.server.api.organization.password.validator;

import static org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n.t_;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n.LOCALE;
import org.bonitasoft.web.toolkit.client.common.texttemplate.Arg;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.AbstractStringValidator;

/**
 * Do not delete: this class can be used by reflection !
 * See 'security.password.validator' in file 'security-config.properties'
 * It is referenced in the documentation as an alternative Password Validation
 *
 * @author Paul AMAR
 */
public class RobustnessPasswordValidator extends AbstractStringValidator {

    @Override
    protected void _check(String password) {
        String regex;

        LOCALE Locale = AbstractI18n.stringToLocale(locale);

        // Check number of digits
        regex = "[0-9]";
        int numberMinOccurences = 3;
        if (numberOfOccurenceOfRegex(regex, password) < numberMinOccurences) {
            addError(t_("Password must contain at least %number% digits", Locale,
                    new Arg("number", numberMinOccurences)));
        }

        // Check number of lower case chars
        regex = "[a-z]";
        numberMinOccurences = 2;
        if (numberOfOccurenceOfRegex(regex, password) < numberMinOccurences) {
            addError(t_("Password must contain at least %number% lower case characters", Locale,
                    new Arg("number", numberMinOccurences)));
        }

        // Check number of upper case chars
        regex = "[A-Z]";
        numberMinOccurences = 2;
        if (numberOfOccurenceOfRegex(regex, password) < numberMinOccurences) {
            addError(t_("Password must contain at least %number% upper case characters", Locale,
                    new Arg("number", numberMinOccurences)));
        }

        // Check number of special chars
        regex = "[~@#\\^\\$&\\*\\(\\)-T_\\+=\\[\\]\\{\\}\\|\\,\\.\\?]";
        numberMinOccurences = 2;
        if (numberOfOccurenceOfRegex(regex, password) < numberMinOccurences) {
            addError(t_("Password must contain at least %number% special characters", Locale,
                    new Arg("number", numberMinOccurences)));
        }

        // Check number of length
        int minimalLength = 10;
        if (password.length() < minimalLength) {
            addError(
                    t_("Password must be at least %number% characters long", Locale, new Arg("number", minimalLength)));
        }
    }

    private int numberOfOccurenceOfRegex(String regex, String password) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);

        int count = 0;
        while (matcher.find())
            count++;

        return count;
    }

}
