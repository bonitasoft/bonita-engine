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

package org.bonitasoft.engine.bdm.validator.rule.composition;

import static org.bonitasoft.engine.bdm.model.field.RelationField.Type.AGGREGATION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.result.StatusCode;
import org.bonitasoft.engine.api.result.StatusContext;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;
import org.bonitasoft.engine.bdm.validator.rule.ValidationRule;

/**
 * @author Danila Mazour
 */
public class AggregationAndCompositionValidationRule extends ValidationRule<BusinessObjectModel, ValidationStatus> {

    public AggregationAndCompositionValidationRule() {
        super(BusinessObjectModel.class);
    }

    @Override
    protected ValidationStatus validate(BusinessObjectModel bom) {
        ValidationStatus validationStatus = new ValidationStatus();
        List<BusinessObject> businessObjects = bom.getBusinessObjects();
        List<BusinessObject> aggregatedBusinessObjects = new ArrayList<>();
        List<BusinessObject> composedBusinessObjects = new ArrayList<>();
        List<Field> fieldList = new ArrayList<>();
        for (BusinessObject bo : businessObjects)
            fieldList.addAll(bo.getFields());
        for (Field field : fieldList) {
            if (field instanceof RelationField) {
                RelationField relationField = (RelationField) field;
                if (relationField.getType() == AGGREGATION) {
                    aggregatedBusinessObjects.add(relationField.getReference());
                }
            }
        }
        composedBusinessObjects.addAll(bom.getReferencedBusinessObjectsByComposition());
        for (BusinessObject composedBo : composedBusinessObjects) {
            if (aggregatedBusinessObjects.contains(composedBo)) {
                validationStatus.addWarning(StatusCode.BUSINESS_OBJECT_USED_IN_COMPOSITION_AND_AGGREGATION,
                        String.format(
                                "The object %s is referenced both in composition and in aggregation. This may lead to runtime errors and"
                                + " may lead to unpredictable behaviour of the AccessControl configuration.",
                        composedBo.getQualifiedName()),
                        Collections.singletonMap(StatusContext.BUSINESS_OBJECT_NAME_KEY, composedBo.getQualifiedName()));
            }
        }
        return validationStatus;
    }
}
