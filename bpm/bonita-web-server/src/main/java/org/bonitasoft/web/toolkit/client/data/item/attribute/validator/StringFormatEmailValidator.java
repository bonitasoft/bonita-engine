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

import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;

/**
 * @author Séverin Moussel
 */
public class StringFormatEmailValidator extends AbstractStringFormatValidator {

    public StringFormatEmailValidator() {
        // RFC 2822 with permissive modification (allow not quoted name) and allow TLD from 2 characters to 32 (32 was arbitrary chosen)
        super("[a-zA-Z0-9!#$%&'*+/=?^T_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^T_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+(?:[a-zA-Z]{2,32})");
    }

    @Override
    protected final void _check(final String attributeValue) {
        if (attributeValue.contains(" ")) {
            addError(defineErrorMessage());
        } else {
            super._check(attributeValue);
        }
    }

    @Override
    protected String defineErrorMessage() {
        return AbstractI18n.t_("%attribute% is not a valid email");
    }

}
