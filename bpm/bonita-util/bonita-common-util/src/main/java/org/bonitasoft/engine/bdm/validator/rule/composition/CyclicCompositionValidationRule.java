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
package org.bonitasoft.engine.bdm.validator.rule.composition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.result.StatusCode;
import org.bonitasoft.engine.api.result.StatusContext;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;
import org.bonitasoft.engine.bdm.validator.rule.ValidationRule;

/**
 * Check that there are no circular references on referenced business objects by composition.
 * 
 * @author Colin PUY
 */
public class CyclicCompositionValidationRule extends ValidationRule<BusinessObjectModel, ValidationStatus> {

    public CyclicCompositionValidationRule() {
        super(BusinessObjectModel.class);
    }

    @Override
    protected ValidationStatus validate(BusinessObjectModel bom) {
        ValidationStatus validationStatus = new ValidationStatus();
        for (BusinessObject bo : bom.getBusinessObjects()) {
            validationStatus.addValidationStatus(validateThatThereIsNoCycleDependencies(bo, new ArrayList<>()));
        }
        return validationStatus;
    }

    private ValidationStatus validateThatThereIsNoCycleDependencies(BusinessObject bo, List<BusinessObject> parentBOs) {
        ValidationStatus validationStatus = new ValidationStatus();
        parentBOs.add(bo);
        Map<String, Serializable> context = new HashMap<>();
        context.put(StatusContext.BDM_ARTIFACT_NAME_KEY, bo.getQualifiedName());
        for (BusinessObject businessObject : bo.getReferencedBusinessObjectsByComposition()) {
            if (parentBOs.contains(businessObject)) {
                context.put(StatusContext.BUSINESS_OBJECT_NAME_KEY, businessObject.getQualifiedName());
                validationStatus.addError(StatusCode.CIRCULAR_COMPOSITION_REFERENCE, String.format(
                        "Business object %s has a circular composition reference to itself or is referenced several times in the object %s",
                        businessObject.getQualifiedName(), bo.getQualifiedName()), context);
            } else {
                validationStatus.addValidationStatus(validateThatThereIsNoCycleDependencies(businessObject, parentBOs));
            }
        }
        return validationStatus;
    }
}
