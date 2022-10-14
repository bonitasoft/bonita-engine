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

/**
 * @author Séverin Moussel
 */
public abstract class AbstractStringValidator extends AbstractCollectionValidator {

    protected String locale = "";

    @Override
    protected final void _check(final String[] attributeValue) {
        for (int i = 0; i < attributeValue.length; i++) {
            this.check(attributeValue[i]);
        }
    }

    /**
     * Function to override to define the checking operation
     *
     * @param attributeValue
     */
    public final void check(final String attributeValue) {
        reset();
        if (attributeValue == null || attributeValue.length() == 0) {
            // Not an error. The null value will be detected by a mandatory validator.
            return;
        }

        this._check(attributeValue);
    }

    protected abstract void _check(String attributeValue);

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
