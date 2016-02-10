/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.builder;

import java.util.List;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.field.SimpleField;
import static com.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static com.bonitasoft.engine.bdm.builder.FieldBuilder.*;

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
        for (final BusinessObject bo : bos) {
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
