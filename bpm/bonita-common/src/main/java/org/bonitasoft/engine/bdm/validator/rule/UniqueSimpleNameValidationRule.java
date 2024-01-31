/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.bdm.validator.rule;

import static org.bonitasoft.engine.api.result.StatusCode.DUPLICATE_BUSINESS_OBJECT_NAME;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Danila Mazour
 */
public class UniqueSimpleNameValidationRule extends ValidationRule<BusinessObjectModel, ValidationStatus> {

    public UniqueSimpleNameValidationRule() {
        super(BusinessObjectModel.class);
    }

    @Override
    public ValidationStatus validate(final BusinessObjectModel bom) {
        final ValidationStatus status = new ValidationStatus();
        List<BusinessObject> businessObjects = bom.getBusinessObjects();
        Set<String> businessObjectNames = new HashSet<>();
        for (BusinessObject businessObject : businessObjects) {
            if (!businessObjectNames.add(businessObject.getSimpleName().toLowerCase())) {
                status.addError(DUPLICATE_BUSINESS_OBJECT_NAME,
                        " There are at least 2 objects in the BDM that are called : " + businessObject.getSimpleName());
            }
        }
        return status;
    }
}
