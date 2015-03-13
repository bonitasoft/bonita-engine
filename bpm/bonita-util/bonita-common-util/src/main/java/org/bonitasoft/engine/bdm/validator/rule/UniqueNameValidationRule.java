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

import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.validator.UniqueNameValidator;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Colin PUY
 */
public class UniqueNameValidationRule extends ValidationRule<BusinessObjectModel> {

    private UniqueNameValidator uniqueNameValidator;

    public UniqueNameValidationRule(UniqueNameValidator uniqueNameValidator) {
        super(BusinessObjectModel.class);
        this.uniqueNameValidator = uniqueNameValidator;
    }

    @Override
    protected ValidationStatus validate(BusinessObjectModel bom) {
        ValidationStatus validationStatus = new ValidationStatus();
        validationStatus.addValidationStatus(uniqueNameValidator.validate(bom.getUniqueConstraints(), "unique contraints"));
        validationStatus.addValidationStatus(uniqueNameValidator.validate(bom.getIndexes(), "indexes"));
        return validationStatus;
    }
}
