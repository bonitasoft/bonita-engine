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
public abstract class AbstractNumericValidator extends AbstractStringValidator {

    @Override
    /**
     * Check if the value is numeric, then delegate to abstract _check(Double)
     */
    protected final void _check(final String attributeValue) {
        Double numericValue = null;
        try {
            numericValue = Double.valueOf(attributeValue);
        } catch (final NumberFormatException e) {
            addError(AbstractI18n.t_("%attribute% must be a numeric value"));
        }

        if (numericValue != null) {
            this._check(numericValue);
        }

    }

    protected abstract void _check(Double attributeValue);

}
