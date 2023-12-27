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
import java.util.Map;

import org.bonitasoft.web.toolkit.client.common.TreeIndexed;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.AbstractStringValidator;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.MandatoryValidator;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.Validator;

/**
 * @author Séverin Moussel
 */
public class ValidatorEngine {

    public static void validateAttribute(final String attributeName, final Map<String, String> values,
            final List<Validator> validators,
            final boolean applyMandatory)
            throws ValidationException {

        final List<ValidationError> errors = new LinkedList<>();

        // Get validators
        if (validators != null) {
            // Check validators
            for (final Validator validator : validators) {
                // force attribute name as it could be different from the one set in the item definition (case of the deploys)
                validator.setAttributeName(attributeName);

                // Check mandatory validator
                if (validator instanceof MandatoryValidator) {
                    if (applyMandatory) {
                        ((MandatoryValidator) validator).check(values.get(attributeName));
                    }
                }
                // Check String based validator
                else if (validator instanceof AbstractStringValidator) {
                    ((AbstractStringValidator) validator).check(values.get(attributeName));
                }
                errors.addAll(validator.getErrors());
            }
        }
        if (errors.size() > 0) {
            throw new ValidationException(errors);
        }
    }

    /**
     * Validate an Item
     */
    public static void validate(final IItem item) throws ValidationException {
        validate(item, true);
    }

    /**
     * Validate an Item
     */
    public static void validate(final IItem item, final boolean applyMandatory) throws ValidationException {
        validate(item.getAttributes(), item.getItemDefinition().getValidators(), applyMandatory);
    }

    /**
     * Validate a Tree
     */
    public static void validate(final TreeIndexed<String> tree, final Map<String, List<Validator>> validators,
            final boolean applyMandatory)
            throws ValidationException {
        validate(tree.getValues(), validators, applyMandatory);
    }

    /**
     * Validate a Map
     */
    public static void validate(final Map<String, String> values, final Map<String, List<Validator>> validators,
            final boolean applyMandatory)
            throws ValidationException {
        final List<ValidationError> errors = new LinkedList<>();
        for (final String attributeName : values.keySet()) {
            try {
                validateAttribute(attributeName, values, validators.get(attributeName), applyMandatory);
            } catch (final ValidationException e) {
                errors.addAll(e.getErrors());
            }
        }
        if (errors.size() > 0) {
            throw new ValidationException(errors);
        }
    }

}
