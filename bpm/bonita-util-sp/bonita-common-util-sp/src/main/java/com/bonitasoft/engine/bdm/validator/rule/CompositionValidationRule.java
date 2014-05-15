/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.validator.rule;

import java.util.ArrayList;
import java.util.List;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.field.Field;
import com.bonitasoft.engine.bdm.model.field.RelationField;
import com.bonitasoft.engine.bdm.model.field.RelationField.Type;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;


public class CompositionValidationRule extends ValidationRule<BusinessObjectModel> {

    public CompositionValidationRule() {
        super(BusinessObjectModel.class);
    }

    @Override
    ValidationStatus validate(BusinessObjectModel bom) {
        ValidationStatus validationStatus = new ValidationStatus();
        List<RelationField> relationFields = getRelationFields(bom);
        if (relationFields.isEmpty()) {
            return validationStatus;
        }
        
        List<BusinessObject> list = new ArrayList<BusinessObject>();
        for (RelationField relationField : relationFields) {
            if (relationField.getType().equals(Type.COMPOSITION)) {
                if (list.contains(relationField.getReference())) {
                    validationStatus.addError("Business object " + relationField.getReference().getQualifiedName() 
                            + " is composed in two business objects");
                } else {
                    list.add(relationField.getReference());
                }
            }
        }
        return validationStatus;
    }
    
    private List<RelationField> getRelationFields(BusinessObjectModel bom) {
        List<RelationField> list = new ArrayList<RelationField>();
        for (BusinessObject bo : bom.getBusinessObjects()) {
            for (Field f : bo.getFields()) {
                if( f instanceof RelationField) {
                    list.add((RelationField) f);
                }
            }
        }
        return list;
    }

}
