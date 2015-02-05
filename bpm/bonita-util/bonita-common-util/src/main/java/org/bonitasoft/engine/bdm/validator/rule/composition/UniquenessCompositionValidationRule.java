/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.bdm.validator.rule.composition;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;
import org.bonitasoft.engine.bdm.validator.rule.ValidationRule;

/**
 * Check that a composite bo is referenced in only one composition
 * 
 * @author Colin PUY
 */
public class UniquenessCompositionValidationRule extends ValidationRule<BusinessObjectModel> {

    public UniquenessCompositionValidationRule() {
        super(BusinessObjectModel.class);
    }

    @Override
    protected ValidationStatus validate(BusinessObjectModel bom) {
        ValidationStatus validationStatus = new ValidationStatus();
        List<BusinessObject> alreadyComposedBOs = new ArrayList<BusinessObject>();
        for (BusinessObject compositeBO : bom.getReferencedBusinessObjectsByComposition()) {
            if (alreadyComposedBOs.contains(compositeBO)) {
                validationStatus.addError("Business object " + compositeBO.getQualifiedName() + " is referenced by composition in two business objects");
            } else {
                alreadyComposedBOs.add(compositeBO);
            }
        }
        return validationStatus;
    }

}
