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

import org.bonitasoft.web.toolkit.client.common.texttemplate.Arg;
import org.bonitasoft.web.toolkit.client.common.texttemplate.TextTemplate;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ValidationError;

/**
 * @author Séverin Moussel
 */
public abstract class Validator {

    private final ArrayList<ValidationError> errors = new ArrayList<>();

    private String attributeName = null;

    /**
     * @param attributeName
     *        the attributeName to set
     */
    public void setAttributeName(final String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * @return the attributeName
     */
    public String getAttributeName() {
        return this.attributeName;
    }

    public final ArrayList<ValidationError> getErrors() {
        return this.errors;
    }

    protected void addError(final String error) {
        this.errors.add(
                new ValidationError(
                        this.attributeName,
                        new TextTemplate(error).toString(new Arg("attribute", "%" + this.attributeName + "%"))));
    }

    public final boolean hasError() {
        return this.errors.size() > 0;
    }

    protected void reset() {
        this.errors.clear();
    }

}
