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

public class BusinessObjectModelBuilder {

    private BusinessObjectModel businessObjectModel = new BusinessObjectModel();

    public static BusinessObjectModelBuilder aBOM() {
        return new BusinessObjectModelBuilder();
    }

    public BusinessObjectModelBuilder withBO(BusinessObject bo) {
        businessObjectModel.addBusinessObject(bo);
        return this;
    }

    public BusinessObjectModel build() {
        return businessObjectModel;
    }

    private BusinessObject buildMyBusinessObject() {
        return aBO("BusinessObject")
                .withField(aStringField("stringField").nullable().build())
                .withField(aBooleanField("booleanField"))
                .withField(aDateField("dateField").notNullable().build())
                .withField(aDoubleField("doubleField").build())
                .withField(anIntegerField("integerField").build())
                .withField(aTextField("textField").build())
                .build();
    }

    public BusinessObjectModel buildDefaultBOM() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(buildMyBusinessObject());
        return bom;
    }

    public BusinessObjectModel buildEmptyBOM() {
        return new BusinessObjectModel();
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

}
