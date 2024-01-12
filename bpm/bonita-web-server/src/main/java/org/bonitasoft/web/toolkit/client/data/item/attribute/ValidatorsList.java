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
package org.bonitasoft.web.toolkit.client.data.item.attribute;

import java.util.LinkedList;
import java.util.List;

import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.Validator;
import org.bonitasoft.web.toolkit.client.ui.utils.ListUtils;

/**
 * @author Séverin Moussel
 */
public class ValidatorsList implements Validable {

    private final List<Validator> validators = new LinkedList<>();

    @Override
    public List<Validator> getValidators() {
        return this.validators;
    }

    @Override
    public ValidatorsList addValidator(final Validator validator) {
        ListUtils.removeFromListByClass(this.validators, validator.getClass().getName(), true);
        this.validators.add(validator);
        return this;
    }

    @Override
    public ValidatorsList addValidators(final List<Validator> validators) {
        for (final Validator validator : validators) {
            addValidator(validator);
        }
        return this;
    }

    @Override
    public ValidatorsList removeValidator(final String validatorClassName) {
        ListUtils.removeFromListByClass(this.validators, validatorClassName);
        return this;
    }

    @Override
    public boolean hasValidator(final String validatorClassName) {
        return getValidator(validatorClassName) != null;
    }

    @Override
    public Validator getValidator(final String validatorClassName) {
        return (Validator) ListUtils.getFromListByClass(this.validators, validatorClassName);
    }
}
