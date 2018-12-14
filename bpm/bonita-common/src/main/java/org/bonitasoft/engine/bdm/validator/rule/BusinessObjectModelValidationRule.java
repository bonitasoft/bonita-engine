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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.api.result.StatusCode;
import org.bonitasoft.engine.api.result.StatusContext;
import org.bonitasoft.engine.bdm.BDMQueryUtil;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 */
public class BusinessObjectModelValidationRule extends ValidationRule<BusinessObjectModel, ValidationStatus> {

    public BusinessObjectModelValidationRule() {
        super(BusinessObjectModel.class);
    }

    @Override
    public ValidationStatus validate(final BusinessObjectModel bom) {
        final ValidationStatus status = new ValidationStatus();
        if (bom.getBusinessObjects().isEmpty()) {
            status.addError(StatusCode.EMPTY_BDM, "Business object model must have at least one business object declared");
        } else {
            validateQueries(bom, status);
        }
        return status;
    }

    private void validateQueries(final BusinessObjectModel bom, final ValidationStatus status) {
        for (final BusinessObject bo : bom.getBusinessObjects()) {
            final List<Query> lazyQueries = BDMQueryUtil.createProvidedQueriesForLazyField(bom, bo);
            final Set<String> lazyQueryNames = new HashSet<>();
            for (final Query query : lazyQueries) {
                lazyQueryNames.add(query.getName());
            }
            Map<String, Serializable> context = new HashMap<>();
            context.put(StatusContext.BUSINESS_OBJECT_NAME_KEY, bo.getQualifiedName());
            for (final Query q : bo.getQueries()) {
                if (lazyQueryNames.contains(q.getName())) {
                    context.put(StatusContext.BDM_ARTIFACT_NAME_KEY, q.getName());
                    status.addError(StatusCode.DUPLICATE_QUERY_NAME,
                            "The query named \"" + q.getName() + "\" already exists for " + bo.getQualifiedName(),
                            context);
                }
            }
        }
    }
}
