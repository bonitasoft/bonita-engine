/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.bdm.validator.rule;

import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 */
public abstract class ValidationRule<T> {

    private Class<T> classToApply;

    public ValidationRule(Class<T> classToApply) {
        this.classToApply = classToApply;
    }
    
    public boolean appliesTo(Object modelElement) {
        return modelElement != null && classToApply.isAssignableFrom(modelElement.getClass());
    }

    protected abstract ValidationStatus validate(T modelElement);
    
    @SuppressWarnings("unchecked")
    public ValidationStatus checkRule(Object modelElement) {
        if (!appliesTo(modelElement)) {
            throw new IllegalArgumentException(this.getClass().getName() + " doesn't handle validation for " + modelElement.getClass().getName());
        }
        return validate((T) modelElement);
    }

}
