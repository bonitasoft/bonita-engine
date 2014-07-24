/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model.builder;

import static com.bonitasoft.engine.bdm.model.builder.BusinessObjectBuilder.aBO;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.aBooleanField;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.aDateField;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.aDoubleField;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.aStringField;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.aTextField;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.anIntegerField;

import java.util.List;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.field.SimpleField;

/**
 * @author Colin PUY
 */
public class BusinessObjectModelBuilder {

    private final BusinessObjectModel businessObjectModel = new BusinessObjectModel();

    public static BusinessObjectModelBuilder aBOM() {
        return new BusinessObjectModelBuilder();
    }

    public BusinessObjectModelBuilder withBO(final BusinessObject bo) {
        businessObjectModel.addBusinessObject(bo);
        return this;
    }
    
    public BusinessObjectModelBuilder withBOs(final BusinessObject... bos) {
        for (BusinessObject bo : bos) {
            businessObjectModel.addBusinessObject(bo);
        }
        return this;
    }

    public BusinessObjectModel build() {
        return businessObjectModel;
    }

    private BusinessObject buildMyBusinessObject() {
        return aBO("BusinessObject").withField(aStringField("stringField").nullable().build()).withField(aBooleanField("booleanField"))
                .withField(aDateField("dateField").notNullable().build()).withField(aDoubleField("doubleField").build())
                .withField(anIntegerField("integerField").build()).withField(aTextField("textField").build()).build();
    }

    public BusinessObjectModel buildDefaultBOM() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(buildMyBusinessObject());
        return bom;
    }

    public BusinessObjectModel buildBOMWithAnEmptyEntity() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(new BusinessObject());
        return bom;
    }

    public BusinessObjectModel buildBOMWithAnEmptyField() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        final BusinessObject businessObject = new BusinessObject();
        businessObject.setQualifiedName("BusinessObject");
        businessObject.addField(new SimpleField());
        bom.addBusinessObject(businessObject);
        return bom;
    }

    public BusinessObjectModel buildBOMWithUniqueConstraint() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        final BusinessObject employee = buildMyBusinessObject();
        employee.addUniqueConstraint("UC_string_double", "stringField", "doubleField");
        bom.addBusinessObject(employee);
        return bom;
    }

    public BusinessObjectModel buildBOMWithQuery() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        final BusinessObject employee = buildMyBusinessObject();
        employee.addQuery("employeeByName", "Select e FROM Employee e WHERE e.name='romain'", List.class.getName());
        bom.addBusinessObject(employee);
        return bom;
    }

    public BusinessObjectModel buildBOMWithIndex() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        final BusinessObject employee = buildMyBusinessObject();
        employee.addIndex("idx_45", "stringField", "doubleField");
        bom.addBusinessObject(employee);
        return bom;
    }
}
