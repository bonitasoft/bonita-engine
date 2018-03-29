/*
 * Copyright (C) 2018 BonitaSoft S.A.
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
 */

package org.bonitasoft.engine.bdm.validator.rule;

import static org.bonitasoft.engine.api.result.StatusCode.MULTIPLE_AGGREGATION_RELATION_TO_ITSELF;
import static org.bonitasoft.engine.bdm.model.field.RelationField.Type.AGGREGATION;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.result.StatusContext;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Danila Mazour
 */
public class MultipleAggregationToItselfValidationRule extends ValidationRule<BusinessObjectModel, ValidationStatus> {

    public MultipleAggregationToItselfValidationRule() {
        super(BusinessObjectModel.class);
    }

    @Override
    protected ValidationStatus validate(BusinessObjectModel bom) {
        ValidationStatus validationStatus = new ValidationStatus();
        for (BusinessObject bo : bom.getBusinessObjects()) {
            List<Field> boFields = bo.getFields();
            for (Field boField : boFields) {
                if (boField instanceof RelationField) {
                    RelationField relationField = (RelationField) boField;
                    String fieldReferenceQualifiedName = relationField.getReference().getQualifiedName();
                    String boQualifiedName = bo.getQualifiedName();
                    if (relationField.getType() == AGGREGATION && fieldReferenceQualifiedName.equals(boQualifiedName) && relationField.isCollection()) {
                        validationStatus.addError(MULTIPLE_AGGREGATION_RELATION_TO_ITSELF, "The object "
                                + boQualifiedName + " is referencing itself in a multiple aggregation relation.",
                                Collections.singletonMap(StatusContext.BUSINESS_OBJECT_NAME_KEY, boQualifiedName));
                    }
                }
            }
        }
        return validationStatus;
    }
}
