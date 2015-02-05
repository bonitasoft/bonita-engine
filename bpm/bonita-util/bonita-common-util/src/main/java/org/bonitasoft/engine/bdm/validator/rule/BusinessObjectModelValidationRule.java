/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.bdm.validator.rule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bdm.BDMQueryUtil;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 */
public class BusinessObjectModelValidationRule extends ValidationRule<BusinessObjectModel> {

    public BusinessObjectModelValidationRule() {
        super(BusinessObjectModel.class);
    }

    @Override
    public ValidationStatus validate(final BusinessObjectModel bom) {
        final ValidationStatus status = new ValidationStatus();
        if (bom.getBusinessObjects().isEmpty()) {
            status.addError("Business object model must have at least one business object declared");
        }
        validateQueries(bom, status);
        return status;
    }

    private void validateQueries(final BusinessObjectModel bom, final ValidationStatus status) {
        for (final BusinessObject bo : bom.getBusinessObjects()) {
            final List<Query> lazyQueries = BDMQueryUtil.createProvidedQueriesForLazyField(bom, bo);
            final Set<String> lazyQueryNames = new HashSet<String>();
            for(final Query query : lazyQueries){
                lazyQueryNames.add(query.getName());
            }
            for (final Query q : bo.getQueries()) {
                if (lazyQueryNames.contains(q.getName())) {
                    status.addError("The query named \"" + q.getName() + "\" already exists for " + bo.getQualifiedName());
                }
            }
        }
    }
}
