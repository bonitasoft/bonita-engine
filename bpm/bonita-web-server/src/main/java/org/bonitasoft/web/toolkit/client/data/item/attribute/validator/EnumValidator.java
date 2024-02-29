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
package org.bonitasoft.web.toolkit.client.data.item.attribute.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;

/**
 * @author Paul AMAR
 */
public class EnumValidator extends AbstractStringValidator {

    private final List<String> listString;

    /**
     * Default Constructor.
     */
    public EnumValidator(final List<String> listString) {
        this.listString = listString;
    }

    /**
     * Default Constructor.
     */
    public EnumValidator(final String... listString) {
        this(new ArrayList<>(Arrays.asList(listString)));
    }

    /*
     * (non-Javadoc)
     * @see
     * org.bonitasoft.web.toolkit.client.data.item.attribute.validator.AbstractStringValidator#_check(java.lang.String)
     */
    @Override
    protected void _check(final String attributeValue) {
        final StringBuilder cleanList = new StringBuilder();
        for (final String s : listString) {
            cleanList.append(s).append(", ");
            if (s.equals(attributeValue)) {
                return;
            }
        }
        addError(AbstractI18n.t_("%attribute% must be one of {%list%}").replace("%list%",
                cleanList.substring(0, cleanList.length() - 2)));

    }

    public void addValue(final String value) {
        listString.add(value);
    }

}
