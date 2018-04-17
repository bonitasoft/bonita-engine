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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.result.StatusCode;
import org.bonitasoft.engine.api.result.StatusContext;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;
import org.bonitasoft.engine.bdm.validator.rule.ValidationRule;

/**
 * Check that a composite bo is referenced in only one composition
 * 
 * @author Colin PUY
 */
public class UniquenessCompositionValidationRule extends ValidationRule<BusinessObjectModel, ValidationStatus> {

    public UniquenessCompositionValidationRule() {
        super(BusinessObjectModel.class);
    }

    @Override
    protected ValidationStatus validate(BusinessObjectModel bom) {
        ValidationStatus validationStatus = new ValidationStatus();
        List<BusinessObject> alreadyComposedBOs = new ArrayList<>();
        for (BusinessObject compositeBO : bom.getReferencedBusinessObjectsByComposition()) {
            if (alreadyComposedBOs.contains(compositeBO)) {
                validationStatus.addError(StatusCode.SEVERAL_COMPOSITION_REFERENCE_FOR_A_BUSINESS_OBJECT,
                        String.format(
                                "Business object %s is referenced by composition in two business objects, or is referenced several times in a single business object",
                                compositeBO.getQualifiedName()),
                        Collections.singletonMap(StatusContext.BUSINESS_OBJECT_NAME_KEY, compositeBO.getQualifiedName()));
            } else {
                alreadyComposedBOs.add(compositeBO);
            }
        }
        return validationStatus;
    }

}
